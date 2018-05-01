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
 */
package io.nuls.protocol.base.handler;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.entity.NodePo;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.network.service.NetworkService;
import io.nuls.poc.service.intf.ConsensusService;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.TransactionEvent;
import io.nuls.protocol.model.Transaction;

import java.util.List;

/**
 * @author Niels
 * @date 2018/1/8
 */
public class NewTxEventHandler extends AbstractEventHandler<TransactionEvent> {

    private static NewTxEventHandler INSTANCE = new NewTxEventHandler();

    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);

    private NewTxEventHandler() {
    }

    public static NewTxEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEvent(TransactionEvent event, String fromId) {

        Transaction tx = event.getEventBody();
        //Log.info("receive new tx {} from {}", tx.getHash().getDigestHex(), fromId);
        if (null == tx) {
            return;
        }
        if (tx.getType() == TransactionConstant.TX_TYPE_COIN_BASE || tx.getType() == TransactionConstant.TX_TYPE_YELLOW_PUNISH || tx.getType() == TransactionConstant.TX_TYPE_RED_PUNISH) {
            return;
        }

        ValidateResult result = tx.verify();
        if (result.isFailed() && result.getErrorCode() != ErrorCode.ORPHAN_TX) {
            if (result.getLevel() == SeverityLevelEnum.NORMAL_FOUL) {
                networkService.removeNode(fromId);
            } else if (result.getLevel() == SeverityLevelEnum.FLAGRANT_FOUL) {
                networkService.blackNode(fromId, NodePo.BLACK);
            }
            return;
        }
        try {
            List<Transaction> waitingList = ledgerService.getWaitingTxList();
            for (int i = waitingList.size()-1; i >= 0; i--) {
                if (waitingList.get(i).getHash().equals(tx.getHash())) {
                    waitingList.remove(i);
                    break;
                }
            }

            result = ledgerService.conflictDetectTx(tx, waitingList);
            if (result.isFailed()) {
                return;
            }
        } catch (Exception e) {
            Log.error(e);
            return;
        }
        Transaction tempTx = temporaryCacheManager.getTx(tx.getHash().getDigestHex());
        if (tempTx != null) {
            consensusService.newTx(tx);
            return;
        }
        temporaryCacheManager.cacheTx(tx);

        boolean isMine = ledgerService.checkTxIsMySend(tx);
        try {
//            if (isMine) {
//                ledgerService.saveLocalTx(tx);
//            }
            consensusService.newTx(tx);
            if (isMine) {
                eventBroadcaster.broadcastAndCache(event);
            } else {
                eventBroadcaster.broadcastHashAndCacheAysn(event, fromId);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

}
