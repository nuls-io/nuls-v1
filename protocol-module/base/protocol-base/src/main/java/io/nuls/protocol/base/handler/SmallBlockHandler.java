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
package io.nuls.protocol.base.handler;

import io.nuls.consensus.poc.protocol.service.ConsensusServiceIntf;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.BlockLog;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.network.entity.Node;
import io.nuls.protocol.base.utils.AssemblyBlockUtil;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.message.SmallBlockMessage;
import io.nuls.protocol.model.SmallBlock;
import io.nuls.protocol.model.TxGroup;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.DownloadService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class SmallBlockHandler extends AbstractMessageHandler<SmallBlockMessage> {

    private ConsensusServiceIntf consensusService = NulsContext.getServiceBean(ConsensusServiceIntf.class);

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);

    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();

    @Override
    public void onMessage(SmallBlockMessage event, Node fromNode) {
        SmallBlock smallBlock = event.getMsgBody();
        if (null == smallBlock) {
            Log.warn("recieved a null smallBlock!");
            return;
        }
        BlockHeader header = smallBlock.getHeader();

        BlockHeader theBlockHeader = blockService.getBlockHeader(header.getHash()).getData();
        if (null != theBlockHeader) {
            return;
        }

        //checkIt
        if ((TimeService.currentTimeMillis() - header.getTime()) > ProtocolConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L) {
            Log.info("It's too late:hash:" + header.getHash() + ", height:" + header.getHeight() + ", packerHex:" + Hex.encode(header.getPackingAddress()));
            return;
        }

        ValidateResult result = header.verify();
        boolean isOrphan = result.getErrorCode() == TransactionErrorCode.ORPHAN_TX || result.getErrorCode() == TransactionErrorCode.ORPHAN_BLOCK;

        BlockLog.debug("recieve new block from(" + fromNode.getId() + "), tx count : " + header.getTxCount() + " , tx pool count : " + consensusService.getMemoryTxs().size() + " , header height:" + header.getHeight() + ", preHash:" + header.getPreHash() + " , hash:" + header.getHash() + ", addressHex:" + Hex.encode(header.getPackingAddress()) +
                "\n and verify block result: " + result.isSuccess() + " , verify message : " + result.getMessage() + " , isOrphan : " + isOrphan);

        if (result.isFailed() && !isOrphan) {
            BlockLog.debug("discard a SmallBlock:" + smallBlock.getHeader().getHash() + ", from:" + fromNode.getId() + " ,reason:" + result.getMessage());
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
                continue;
            }
        }
        if (!needHashList.isEmpty()) {
            TxGroup txGroup = this.downloadService.downloadTxGroup(needHashList, fromNode).getData();
            if (null == txGroup) {
                Log.warn("get txgroup failed!block height:{},node:{},blockHash:{}", header.getHeight(), fromNode.getId(), header.getHash());
                return;
            }
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
//        boolean needForward = blockManager.addBlock(block, true, fromId);

        consensusService.newBlock(block, fromNode);
    }
}
