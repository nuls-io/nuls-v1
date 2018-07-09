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

import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.BlockLog;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.*;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.message.bus.handler.AbstractMessageHandler;
import io.nuls.network.model.Node;
import io.nuls.protocol.base.cache.SmallBlockDuplicateRemoval;
import io.nuls.protocol.base.utils.AssemblyBlockUtil;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.message.GetTxGroupRequest;
import io.nuls.protocol.message.SmallBlockMessage;
import io.nuls.protocol.model.GetTxGroupParam;
import io.nuls.protocol.model.SmallBlock;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.TransactionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author facjas
 */
public class SmallBlockHandler extends AbstractMessageHandler<SmallBlockMessage> {

    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);

    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();
    private TransactionService transactionService = NulsContext.getServiceBean(TransactionService.class);

    @Override
    public void onMessage(SmallBlockMessage event, Node fromNode) {
        SmallBlock smallBlock = event.getMsgBody();
        if (null == smallBlock) {
            Log.warn("recieved a null smallBlock!");
            return;
        }

        BlockHeader header = smallBlock.getHeader();

        System.out.println("下载到pre：" + header.getHeight() + " ," + header.getHash() + ", 来源：" + fromNode.getIp());
        if (!SmallBlockDuplicateRemoval.needProcess(header.getHash())) {
            return;
        }


        BlockHeader theBlockHeader = blockService.getBlockHeader(header.getHash()).getData();
        if (null != theBlockHeader) {
            return;
        }

        ValidateResult result = header.verify();
        boolean isOrphan = result.getErrorCode() == TransactionErrorCode.ORPHAN_TX || result.getErrorCode() == TransactionErrorCode.ORPHAN_BLOCK;
        System.out.println("下载到：" + header.getHeight() + " ," + header.getHash() + ", 来源：" + fromNode.getIp());
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
                tx = transactionService.getTx(hash);
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
            GetTxGroupRequest request = new GetTxGroupRequest();
            GetTxGroupParam param = new GetTxGroupParam();
            param.setTxHashList(needHashList);
            request.setMsgBody(param);
            Result sendResult = this.messageBusService.sendToNode(request, fromNode, true);
            if (sendResult.isFailed()) {
                Log.warn("get tx group failed,height:" + header.getHeight());
            } else {
                NulsDigestData requestHash = null;
                try {
                    requestHash = NulsDigestData.calcDigestData(request.getMsgBody().serialize());
                } catch (IOException e) {
                    Log.error(e);
                    return;
                }
                temporaryCacheManager.cacheSmallBlockWithRequest(requestHash, smallBlock);
            }
            return;
        }

        Block block = AssemblyBlockUtil.assemblyBlock(header, txMap, smallBlock.getTxHashList());
        consensusService.newBlock(block, fromNode);
    }
}
