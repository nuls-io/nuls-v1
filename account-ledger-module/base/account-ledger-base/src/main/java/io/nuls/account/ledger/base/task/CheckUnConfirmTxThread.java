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
package io.nuls.account.ledger.base.task;


import io.nuls.account.ledger.base.util.TransactionTimeComparator;
import io.nuls.account.ledger.base.util.TxInfoComparator;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.ledger.storage.service.AccountLedgerStorageService;
import io.nuls.account.ledger.storage.service.UnconfirmedTransactionStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.service.TransactionService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class CheckUnConfirmTxThread implements Runnable {

    @Autowired
    private AccountLedgerService accountLedgerService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LedgerService ledgerService;

    @Autowired
    private AccountLedgerService AccountLedgerService;

    @Autowired
    UnconfirmedTransactionStorageService unconfirmedTransactionStorageService;

    @Autowired
    private AccountLedgerStorageService accountLedgerStorageService;

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(60000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            List<Transaction> list = accountLedgerService.getAllUnconfirmedTransaction().getData();
            Collections.sort(list, TransactionTimeComparator.getInstance());

            if (list != null || list.size() > 0) {
                if (list.get(0).getTime() - TimeService.currentTimeMillis() > 120000L) {
                    Log.info("earliest unconfirmed tx :" + (list.get(0).getTime() - TimeService.currentTimeMillis()));
                    continue;
                }

                for (Transaction tx : list) {
                    Result result = verifyTransaction(tx);
                    if (result.isSuccess()) {
                        result = reBroadcastTransaction(tx);
                        if (result.isFailed()) {
                            Log.info("reBroadcastTransaction tx error");
                        }
                    } else {
                        deleteUnconfirmedTransaction(tx);
                    }
                }
            }
        }
    }

    private void deleteUnconfirmedTransaction(Transaction tx) {

        unconfirmedTransactionStorageService.deleteUnconfirmedTx(tx.getHash());
        deleteOutputofTransaction(tx);
    }

    private void deleteOutputofTransaction(Transaction tx) {
        List<Coin> tos = tx.getCoinData().getTo();
        for (int i = 0; i < tos.size(); i++) {
            Coin to = tos.get(i);
            try {
                byte[] outKey = org.spongycastle.util.Arrays.concatenate(to.getOwner(), tx.getHash().serialize(), new VarInt(i).encode());
                accountLedgerStorageService.deleteUTXO(outKey);
            } catch (IOException e) {
                Log.info("delete unconfirmed output error");
            }
        }
    }


    private Result reBroadcastTransaction(Transaction tx) {
        Result sendResult = transactionService.broadcastTx(tx);
        if (sendResult.isFailed()) {
            return sendResult;
        }
        return Result.getSuccess();
    }

    private Result verifyTransaction(Transaction tx) {
        Result result = tx.verify();
        if (result.isFailed()) {
            return result;
        }
        result = ledgerService.verifyCoinData(tx, AccountLedgerService.getAllUnconfirmedTransaction().getData());

        if (result.isFailed()) {
            return result;
        }
        return Result.getSuccess();
    }
}
