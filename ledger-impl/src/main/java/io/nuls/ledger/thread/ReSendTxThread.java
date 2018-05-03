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
package io.nuls.ledger.thread;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.db.dao.TransactionLocalDataService;
import io.nuls.db.dao.UtxoInputDataService;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.ledger.service.impl.LedgerCacheService;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.TransactionEvent;
import io.nuls.protocol.model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/13
 */
public class ReSendTxThread implements Runnable {
    private static final ReSendTxThread INSTANCE = new ReSendTxThread();

    private ReSendTxThread() {
    }

    public static ReSendTxThread getInstance() {
        return INSTANCE;
    }

    private EventBroadcaster eventBroadcaster;

    private LedgerService ledgerService;

    private LedgerCacheService ledgerCacheService;

    private TransactionLocalDataService localDataService;

    private boolean stop;

    @Override
    public void run() {
        stop = false;
        while (!stop) {
            try {
                Thread.sleep(2 * DateUtil.MINUTE_TIME);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            try {
                reSendLocalTx();
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }

    private void reSendLocalTx() throws NulsException {
        List<Transaction> txList = getLedgerCacheService().getUnconfirmTxList();
        List<Transaction> helpList = new ArrayList<>();
        for (Transaction tx : txList) {
            if (TimeService.currentTimeMillis() - tx.getTime() < DateUtil.MINUTE_TIME * 2) {
                continue;
            }
            ValidateResult result = getLedgerService().verifyTx(tx, helpList);
            if (result.isFailed()) {
                getLocalDataService().deleteUnCofirmTx(tx.getHash().getDigestHex());
                ledgerCacheService.removeLocalTx(tx.getHash().getDigestHex());
                continue;
            }
            Transaction transaction = getLedgerService().getTx(tx.getHash());
            if (transaction != null) {
                getLocalDataService().deleteUnCofirmTx(tx.getHash().getDigestHex());
                ledgerCacheService.removeLocalTx(tx.getHash().getDigestHex());
                continue;
            }
            helpList.add(tx);
            TransactionEvent event = new TransactionEvent();
            event.setEventBody(tx);
            getEventBroadcaster().publishToLocal(event);
        }
    }

    public void stop() {
        this.stop = true;
    }

    public EventBroadcaster getEventBroadcaster() {
        if (eventBroadcaster == null) {
            eventBroadcaster = NulsContext.getServiceBean(EventBroadcaster.class);
        }
        return eventBroadcaster;
    }

    public LedgerService getLedgerService() {
        if (ledgerService == null) {
            ledgerService = NulsContext.getServiceBean(LedgerService.class);
        }
        return ledgerService;
    }

    public LedgerCacheService getLedgerCacheService() {
        if (ledgerCacheService == null) {
            ledgerCacheService = NulsContext.getServiceBean(LedgerCacheService.class);
        }
        return ledgerCacheService;
    }

    public TransactionLocalDataService getLocalDataService() {
        if (localDataService == null) {
            localDataService = NulsContext.getServiceBean(TransactionLocalDataService.class);
        }
        return localDataService;
    }
}
