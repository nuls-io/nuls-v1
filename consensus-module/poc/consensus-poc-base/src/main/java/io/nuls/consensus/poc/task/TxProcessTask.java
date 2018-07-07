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

package io.nuls.consensus.poc.task;

import io.nuls.consensus.poc.cache.TxMemoryPool;
import io.nuls.consensus.poc.container.TxContainer;
import io.nuls.consensus.poc.storage.service.TransactionCacheStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.util.LedgerUtil;
import io.nuls.protocol.utils.TransactionTimeComparator;

import java.util.*;

/**
 * @author: Niels Wang
 * @date: 2018/7/5
 */
public class TxProcessTask implements Runnable {

    private TxMemoryPool pool = TxMemoryPool.getInstance();

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private TransactionCacheStorageService transactionCacheStorageService = NulsContext.getServiceBean(TransactionCacheStorageService.class);

    private TransactionTimeComparator txComparator = TransactionTimeComparator.getInstance();

    private Map<String, Coin> temporaryToMap = new HashMap<>();
    private Set<String> temporaryFromSet = new HashSet<>();

    private Set<NulsDigestData> verifySuccessHashs = new HashSet<>();

    int count = 0;
    int size = 0;

    @Override
    public void run() {
        try {
            doTask();
        } catch (Exception e) {
            Log.error(e);
        }
        System.out.println("count: " + count + " , size : " + size);
    }

    private void doTask() {
        Transaction tx = null;
        while((tx = transactionCacheStorageService.pollTx()) != null) {
            size++;
            processTx(tx);
        }
    }

    private void processTx(Transaction tx) {
        try {
            Result result = tx.verify();
            if(result.isFailed()) {
                return;
            }

//            Transaction tempTx = ledgerService.getTx(tx.getHash());
//            if(tempTx != null) {
//                return;
//            }

            ValidateResult validateResult = ledgerService.verifyCoinData(tx, temporaryToMap, temporaryFromSet);
            if (validateResult.isSuccess()) {
                pool.add(tx, false);

                List<Coin> fromCoins = tx.getCoinData().getFrom();
                for(Coin coin : fromCoins) {
                    String key = LedgerUtil.asString(coin.getOwner());
                    temporaryFromSet.remove(key);
                    temporaryToMap.remove(key);
                }

                verifySuccessHashs.add(tx.getHash());
                count++;
            } else if (validateResult.getErrorCode().equals(TransactionErrorCode.ORPHAN_TX)) {
                processOrphanTx(tx);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void processOrphanTx(Transaction tx) throws NulsException {
        //获取相关交易
        List<Coin> fromCoins = tx.getCoinData().getFrom();
        Set<NulsDigestData> fromHashSets = new HashSet<>();
        for(Coin coin : fromCoins) {
            byte[] hashBytes = LedgerUtil.getTxHashBytes(coin.getOwner());
            NulsDigestData txHash = new NulsDigestData();
            txHash.parse(hashBytes, 0);
            fromHashSets.add(txHash);
        }

        List<NulsDigestData> notFoundHashs = new ArrayList<>();
        for(NulsDigestData hash : fromHashSets) {
            Transaction fromTx = ledgerService.getTx(hash);
            if(fromTx == null) {
                notFoundHashs.add(hash);
            }
        }

        if(notFoundHashs.size() == 0) {
            count ++;
            return;
        }

        // find in cache
        boolean hashFound = true;
        for(NulsDigestData hash : notFoundHashs) {
            if(!verifySuccessHashs.contains(hash)) {
                hashFound = false;
                break;
            }
        }

        if(!hashFound) {
            transactionCacheStorageService.putTx(tx);
        }
    }
}
