/**
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
 */
package io.nuls.consensus.event.handler;

import io.nuls.consensus.cache.manager.block.TemporaryCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.entity.RedPunishData;
import io.nuls.consensus.entity.TxGroup;
import io.nuls.consensus.event.TxGroupEvent;
import io.nuls.consensus.event.notice.AssembledBlockNotice;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.consensus.thread.ConsensusMeetingRunner_New;
import io.nuls.consensus.utils.DownloadDataUtils;
import io.nuls.core.chain.entity.*;
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
public class TxGroupHandler extends AbstractEventHandler<TxGroupEvent> {
    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private DownloadDataUtils downloadDataUtils = DownloadDataUtils.getInstance();
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private BlockManager blockManager = BlockManager.getInstance();

    @Override
    public void onEvent(TxGroupEvent event, String fromId) {
        TxGroup txGroup = event.getEventBody();
        BlockHeader header = temporaryCacheManager.getBlockHeader(event.getEventBody().getBlockHash().getDigestHex());
        if (header == null) {
            return;
        }
        SmallBlock smallBlock = temporaryCacheManager.getSmallBlock(header.getHash().getDigestHex());
        if (null == smallBlock) {
            return;
        }
        Block block = new Block();
        block.setHeader(header);
        List<Transaction> txs = new ArrayList<>();
        for (NulsDigestData txHash : smallBlock.getTxHashList()) {
            Transaction tx = txCacheManager.getTx(txHash);
            if (null == tx) {
                tx = txGroup.getTx(txHash.getDigestHex());
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
                ConsensusMeetingRunner_New.putPunishData(data);
                networkService.blackNode(fromId, NodePo.BLACK);
            } else {
                networkService.removeNode(fromId, NodePo.YELLOW);
            }
            return;
        }
        blockManager.addBlock(block, false, fromId);
        downloadDataUtils.removeTxGroup(block.getHeader().getHash().getDigestHex());
        AssembledBlockNotice notice = new AssembledBlockNotice();
        notice.setEventBody(header);
        eventBroadcaster.publishToLocal(notice);

    }
}
