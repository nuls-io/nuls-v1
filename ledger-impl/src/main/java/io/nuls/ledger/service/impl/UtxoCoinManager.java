/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.ledger.service.impl;

import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.TransactionLocalDataService;
import io.nuls.db.dao.UtxoOutputDataService;
import io.nuls.db.entity.TransactionLocalPo;
import io.nuls.db.entity.UtxoOutputPo;
import io.nuls.ledger.entity.*;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.util.UtxoTransactionTool;
import io.nuls.ledger.util.UtxoTransferTool;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.model.Na;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * cache and operate all unspend utxo
 */
public class UtxoCoinManager {

    private static UtxoCoinManager instance = new UtxoCoinManager();

    private UtxoCoinManager() {

    }

    public static UtxoCoinManager getInstance() {
        return instance;
    }

    private UtxoOutputDataService outputDataService = NulsContext.getServiceBean(UtxoOutputDataService.class);

    private TransactionLocalDataService localDataService = NulsContext.getServiceBean(TransactionLocalDataService.class);

    private LedgerCacheService ledgerCacheService = LedgerCacheService.getInstance();

    private Lock lock = new ReentrantLock();

    public void cacheAllUnSpendUtxo() {
        List<UtxoOutputPo> utxoOutputPos = outputDataService.getAllUnSpend();
        Set<String> addressSet = new HashSet<>();

        for (int i = 0; i < utxoOutputPos.size(); i++) {
            UtxoOutputPo po = utxoOutputPos.get(i);
            UtxoOutput output = UtxoTransferTool.toOutput(po);
            ledgerCacheService.putUtxo(output.getKey(), output);
            addressSet.add(po.getAddress());
        }

//        for (String str : addressSet) {
//            UtxoTransactionTool.getInstance().calcBalance(str, false);
//        }
    }

    /**
     * Utxo is used to ensure that each transaction will not double
     */
    public List<UtxoOutput> getAccountUnSpend(String address, Na value) {
        List<UtxoOutput> unSpends = new ArrayList<>();
        try {
            UtxoBalance balance = (UtxoBalance) ledgerCacheService.getBalance(address);
            if (balance == null) {
                return unSpends;
            }

            List<UtxoOutput> outputList = ledgerCacheService.getUnSpends(address);
            filterUtxoByLocalTxs(address, outputList);

            Na amount = Na.ZERO;
            boolean enough = false;
            for (UtxoOutput output : outputList) {
                unSpends.add(output);
                amount = amount.add(Na.valueOf(output.getValue()));
                if (amount.isGreaterOrEquals(value)) {
                    enough = true;
                    break;
                }
            }
            if (!enough) {
                unSpends = new ArrayList<>();
            }
        } catch (Exception e) {
            Log.error(e);
            unSpends = new ArrayList<>();
        }
        return unSpends;
    }

    public List<UtxoOutput> getAccountsUnSpend(List<String> addressList, Na value) {
        List<UtxoOutput> unSpends = new ArrayList<>();
        try {
            //check use-able is enough , find unSpend utxo
            Na amount = Na.ZERO;
            boolean enough = false;
            for (String address : addressList) {
                List<UtxoOutput> outputList = ledgerCacheService.getUnSpends(address);

                filterUtxoByLocalTxs(address, outputList);
                if (outputList.isEmpty()) {
                    continue;
                }
                for (UtxoOutput output : outputList) {
                    if(output.isLocked()){
                        continue;
                    }
                    unSpends.add(output);
                    amount = amount.add(Na.valueOf(output.getValue()));
                    if (amount.isGreaterOrEquals(value)) {
                        enough = true;
                        break;
                    }
                }
                if (enough) {
                    break;
                }
            }
            if (!enough) {
                unSpends = new ArrayList<>();
            }
        } catch (Exception e) {
            Log.error(e);
            unSpends = new ArrayList<>();
        }
        return unSpends;
    }

    public List<AbstractCoinTransaction> getLocalUnConfirmTxs() {
        List<AbstractCoinTransaction> localTxs = new ArrayList<>();
        try {
            List<TransactionLocalPo> poList = localDataService.getUnConfirmTxs();
            for (TransactionLocalPo localPo : poList) {
                AbstractCoinTransaction tx = (AbstractCoinTransaction) UtxoTransferTool.toTransaction(localPo);
                localTxs.add(tx);
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return localTxs;
    }


    public void filterUtxoByLocalTxs(String address, List<UtxoOutput> unSpends) {
        List<AbstractCoinTransaction> localTxs = getLocalUnConfirmTxs();
        if (localTxs.isEmpty()) {
            return;
        }

        Set<String> inputKeySet = new HashSet<>();
        for (AbstractCoinTransaction tx : localTxs) {
            UtxoData utxoData = (UtxoData) tx.getCoinData();

            for(int i=0;i<utxoData.getOutputs().size();i++) {
                UtxoOutput output = utxoData.getOutputs().get(i);
                if (output.getAddress().equals(address)) {
                    if(!output.isLocked()) {
                        unSpends.add(output);
                    }
                }
            }
//            for (UtxoOutput output : utxoData.getOutputs()) {
//                if (output.getAddress().equals(address)) {
//                    unSpends.add(output);
//                }
//            }
            for (UtxoInput input : utxoData.getInputs()) {
                inputKeySet.add(input.getKey());
            }
        }

        for (int i = unSpends.size() - 1; i >= 0; i--) {
            UtxoOutput output = unSpends.get(i);
            if (inputKeySet.contains(output.getKey())) {
                unSpends.remove(i);
            }
        }
    }
}
