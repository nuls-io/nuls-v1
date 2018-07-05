/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.protocol.base.download.smblock;

import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.BlockLog;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.*;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.cache.ProtocolCacheHandler;
import io.nuls.protocol.base.utils.AssemblyBlockUtil;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.message.GetSmallBlockMessage;
import io.nuls.protocol.model.SmallBlock;
import io.nuls.protocol.model.TxGroup;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.DownloadService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author: Niels Wang
 * @date: 2018/6/29
 */
public class SmallBlockDownloadProcessor implements Runnable {

    private static final SmallBlockDownloadProcessor INSTANCE = new SmallBlockDownloadProcessor();

    private BlockingQueue<SmallBlockContainer> queue = new LinkedBlockingDeque<>();

    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);
    protected MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);

    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();

    private SmallBlockDownloadProcessor() {
    }

    public static SmallBlockDownloadProcessor getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        while (true) {
            try {
                process();
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }

    private void process() {

        SmallBlock smallBlock = null;
        Future<SmallBlock> future;
        NulsDigestData blockhash = null;
        Node fromNode;
        try {
            SmallBlockContainer container = queue.take();
            if (null == container || null == container.getFuture() || null == container.getBlockHash()) {
                return;
            }
            future = container.getFuture();
            blockhash = container.getBlockHash();
            fromNode = container.getNode();
        } catch (Exception e) {
            Log.error(e);
            return;
        }
        int i = 0;
        while (i < 3) {
            i++;
            if (!fromNode.isHandShake()) {
                break;
            }
            try {
                smallBlock = future.get(5, TimeUnit.SECONDS);
                ProtocolCacheHandler.removeSmallBlockFuture(blockhash);
            } catch (Exception e) {
                if (!fromNode.isHandShake()) {
                    break;
                }
                future = ProtocolCacheHandler.addGetSmallBlockRequest(blockhash);
                GetSmallBlockMessage getSmallBlockMessage = new GetSmallBlockMessage();
                getSmallBlockMessage.setMsgBody(blockhash);
                Result result = messageBusService.sendToNode(getSmallBlockMessage, fromNode, false);
                if (result.isFailed()) {
                    ProtocolCacheHandler.removeSmallBlockFuture(blockhash);
                    continue;
                }
            }
        }
        if (smallBlock == null) {
            ProtocolCacheHandler.removeSmallBlockFuture(blockhash);
            return;
        }
        BlockHeader header = smallBlock.getHeader();
        BlockHeader theBlockHeader = blockService.getBlockHeader(header.getHash()).getData();
        if (null != theBlockHeader) {
            return;
        }

        ValidateResult result = header.verify();
        boolean isOrphan = result.getErrorCode() == TransactionErrorCode.ORPHAN_TX || result.getErrorCode() == TransactionErrorCode.ORPHAN_BLOCK;

        BlockLog.debug("recieve new block from(" + fromNode.getId() + "), tx count : " + header.getTxCount() + " , tx pool count : " + consensusService.getMemoryTxs().size() + " , header height:" + header.getHeight() + ", preHash:" + header.getPreHash() + " , hash:" + header.getHash() + ", addressHex:" + Hex.encode(header.getPackingAddress()) +
                "\n and verify block result: " + result.isSuccess() + " , verify message : " + result.getMsg() + " , isOrphan : " + isOrphan);

        if (result.isFailed() && !isOrphan) {
            BlockLog.debug("discard a SmallBlock:" + smallBlock.getHeader().getHash() + ", from:" + fromNode.getId() + " ,reason:" + result.getMsg());
            return;
        }
        Map<NulsDigestData, Transaction> txMap = new HashMap<>();
        for (Transaction tx : smallBlock.getSubTxList()) {
            txMap.put(tx.getHash(), tx);
        }
        List<NulsDigestData> needHashList = new ArrayList<>();
        for (NulsDigestData hash : smallBlock.getTxHashList()) {
            Transaction tx = txMap.get(hash);
            if (null == tx) {
                tx = temporaryCacheManager.getTx(hash);
                if (tx != null) {
                    smallBlock.getSubTxList().add(tx);
                    txMap.put(hash, tx);
                }
            }
            if (null == tx) {
                needHashList.add(hash);
            }
        }
        if (!needHashList.isEmpty()) {
            Log.info("block height : " + header.getHeight() + ", tx count : " + header.getTxCount() + " , get group tx of " + needHashList.size());
            TxGroup txGroup = this.downloadService.downloadTxGroup(needHashList, fromNode).getData();
            if (null == txGroup) {
                Log.warn("get txgroup failed!block height:{},node:{},blockHash:{}", header.getHeight(), fromNode.getId(), header.getHash());
                return;
            }
            Log.info("block height : " + header.getHeight() + " get group tx success ");

            for (NulsDigestData hash : needHashList) {
                Transaction tx = txGroup.getTx(hash);
                if (null == tx) {
                    Log.warn("get txgroup wrong!block height:{},node:{},blockHash:{}", header.getHeight(), fromNode.getId(), header.getHash());
                    return;
                }
                txMap.put(tx.getHash(), tx);
            }
        }

        Block block = AssemblyBlockUtil.assemblyBlock(header, txMap, smallBlock.getTxHashList());
        consensusService.newBlock(block, fromNode);
    }

    public void offer(SmallBlockContainer container) {
        queue.offer(container);
    }
}
