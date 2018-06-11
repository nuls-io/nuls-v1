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


import io.nuls.account.ledger.base.manager.BalanceManager;
import io.nuls.account.ledger.base.service.TransactionInfoService;
import io.nuls.account.ledger.base.util.AccountLegerUtils;
import io.nuls.account.ledger.service.AccountLedgerService;
import io.nuls.account.ledger.storage.po.TransactionInfoPo;
import io.nuls.account.ledger.storage.service.LocalUtxoStorageService;
import io.nuls.account.ledger.storage.service.UnconfirmedTransactionStorageService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.db.model.Entry;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.service.TransactionService;

import java.io.IOException;
import java.util.*;

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
    private UnconfirmedTransactionStorageService unconfirmedTransactionStorageService;

    @Autowired
    private LocalUtxoStorageService localUtxoStorageService;

    @Autowired
    private BalanceManager balanceManager;

    @Autowired
    private TransactionInfoService transactionInfoService;

    @Override
    public void run() {
        try {
            doTask();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void doTask() {
        List<Transaction> list = accountLedgerService.getAllUnconfirmedTransaction().getData();
        if (list == null || list.size() == 0) {
            return;
        }

        Map<String, Coin> toMaps = new HashMap<>();
        Set<String> fromSet = new HashSet<>();
        for (Transaction tx : list) {
            if (TimeService.currentTimeMillis() - tx.getTime() < 120000L) {
                return;
            }
            Result result = verifyTransaction(tx, toMaps, fromSet);
            if (result.isSuccess()) {
                result = reBroadcastTransaction(tx);
                if (result.isFailed()) {
                    Log.info("reBroadcastTransaction tx error");
                }
            } else {
                deleteUnconfirmedTransaction(tx);
                List<byte[]> addresses = tx.getAllRelativeAddress();
                for (byte[] address : addresses) {
                    if (AccountLegerUtils.isLocalAccount(address)) {
                        balanceManager.refreshBalance(address);
                    }
                }

            }
        }
    }

    private void deleteUnconfirmedTransaction(Transaction tx) {
        unconfirmedTransactionStorageService.deleteUnconfirmedTx(tx.getHash());
        TransactionInfoPo txInfoPo = new TransactionInfoPo(tx);
        transactionInfoService.deleteTransactionInfo(txInfoPo);
        rollbackUtxo(tx);
    }

    private void rollbackUtxo(Transaction tx) {
        if (tx == null) {
            return;
        }

        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // save - from
            List<Coin> froms = coinData.getFrom();
            List<Entry<byte[], byte[]>> fromList = new ArrayList<>();
            byte[] fromSource;
            byte[] utxoFromSource;
            byte[] fromIndex;
            Transaction sourceTx;
            Coin fromCoin;
            for (Coin from : froms) {
                fromSource = from.getOwner();
                utxoFromSource = new byte[tx.getHash().size()];
                fromIndex = new byte[fromSource.length - utxoFromSource.length];
                System.arraycopy(fromSource, 0, utxoFromSource, 0, tx.getHash().size());
                System.arraycopy(fromSource, tx.getHash().size(), fromIndex, 0, fromIndex.length);
                try {
                    sourceTx = ledgerService.getTx(NulsDigestData.fromDigestHex(Hex.encode(utxoFromSource)));
                } catch (Exception e) {
                    continue;
                }
                if (sourceTx == null) {
                    continue;
                }
                try {
                    fromCoin = sourceTx.getCoinData().getTo().get((int) new VarInt(fromIndex, 0).value);

                    if (!AccountLegerUtils.isLocalAccount(fromCoin.getOwner())) {
                        continue;
                    }

                    fromList.add(new Entry<>(from.getOwner(), fromCoin.serialize()));
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }

            // delete utxo - to
            List<Coin> tos = coinData.getTo();
            List<byte[]> toList = new ArrayList<>();
            Coin toCoin;
            byte[] outKey;
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    toCoin = tos.get(i);
                    if (!AccountLegerUtils.isLocalAccount(toCoin.getOwner())) {
                        continue;
                    }
                    outKey = org.spongycastle.util.Arrays.concatenate(tx.getHash().serialize(), new VarInt(i).encode());
                    toList.add(outKey);
                } catch (IOException e) {
                    Log.info("delete unconfirmed output error");
                    throw new NulsRuntimeException(e);
                }
            }
            localUtxoStorageService.batchSaveAndDeleteUTXO(fromList, toList);
        }
    }


    private Result reBroadcastTransaction(Transaction tx) {
        Result sendResult = transactionService.broadcastTx(tx);
        if (sendResult.isFailed()) {
            return sendResult;
        }
        return Result.getSuccess();
    }

    private Result verifyTransaction(Transaction tx, Map<String, Coin> toMaps, Set<String> fromSet) {
        Result result = tx.verify();
        if (result.isFailed()) {
            return result;
        }
        result = ledgerService.verifyCoinData(tx, toMaps, fromSet);

        if (result.isFailed()) {
            return result;
        }
        return Result.getSuccess();
    }
}
