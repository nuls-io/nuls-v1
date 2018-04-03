/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.event.handler;

import io.nuls.consensus.cache.manager.block.TemporaryCacheManager;
import io.nuls.consensus.cache.manager.tx.OrphanTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.entity.GetTxGroupParam;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.consensus.event.GetTxGroupRequest;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.consensus.utils.DownloadDataUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.entity.NodePo;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class SmallBlockHandler extends AbstractEventHandler<SmallBlockEvent> {
    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();
    private ReceivedTxCacheManager receivedTxCacheManager = ReceivedTxCacheManager.getInstance();
    private OrphanTxCacheManager orphanTxCacheManager = OrphanTxCacheManager.getInstance();
    private DownloadDataUtils downloadDataUtils = DownloadDataUtils.getInstance();
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private BlockManager blockManager = BlockManager.getInstance();
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);

    @Override
    public void onEvent(SmallBlockEvent event, String fromId) {
        ValidateResult result = event.getEventBody().verify();
        if (result.isFailed()) {
            return;
        }
        temporaryCacheManager.cacheSmallBlock(event.getEventBody());
        downloadDataUtils.removeSmallBlock(event.getEventBody().getBlockHash().getDigestHex());
        GetTxGroupRequest request = new GetTxGroupRequest();
        GetTxGroupParam param = new GetTxGroupParam();
        param.setBlockHash(event.getEventBody().getBlockHash());
        for (NulsDigestData hash : event.getEventBody().getTxHashList()) {
            if (!receivedTxCacheManager.txExist(hash) && !orphanTxCacheManager.txExist(hash)) {
                param.addHash(hash);
            }
        }
        if (param.getTxHashList().isEmpty()) {
            BlockHeader header = temporaryCacheManager.getBlockHeader(event.getEventBody().getBlockHash().getDigestHex());
            if (null == header) {
                return;
            }
            Block block = new Block();
            block.setHeader(header);
            List<Transaction> txs = new ArrayList<>();
            for (NulsDigestData txHash : event.getEventBody().getTxHashList()) {
                Transaction tx = receivedTxCacheManager.getTx(txHash);

                if (null == tx) {
                    tx = orphanTxCacheManager.getTx(txHash);
                }
                if (null == tx) {
                    throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
                }
                txs.add(tx);
            }
            block.setTxs(txs);
            ValidateResult<RedPunishData> vResult = block.verify();
            if (null == vResult || (vResult.isFailed() && vResult.getErrorCode() != ErrorCode.ORPHAN_TX)) {
                if (vResult.getLevel() == SeverityLevelEnum.FLAGRANT_FOUL) {
                    RedPunishData data = vResult.getObject();
                    ConsensusMeetingRunner.putPunishData(data);
                    networkService.blackNode(fromId, NodePo.BLACK);
                } else {
                    networkService.removeNode(fromId, NodePo.YELLOW);
                }
                return;
            }
            blockManager.addBlock(block, false, fromId);
            downloadDataUtils.removeTxGroup(block.getHeader().getHash().getDigestHex());

            BlockHeaderEvent headerEvent = new BlockHeaderEvent();
            headerEvent.setEventBody(header);
            eventBroadcaster.broadcastHashAndCacheAysn(headerEvent, false, fromId);
            return;
        }
        request.setEventBody(param);
        this.eventBroadcaster.sendToNode(request, fromId);
    }
}
