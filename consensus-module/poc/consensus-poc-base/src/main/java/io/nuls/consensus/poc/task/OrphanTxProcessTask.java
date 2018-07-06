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
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.utils.TransactionTimeComparator;

import java.util.*;

/**
 * @author: Niels Wang
 * @date: 2018/7/5
 */
public class OrphanTxProcessTask implements Runnable {

    private TxMemoryPool pool = TxMemoryPool.getInstance();

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            Log.error(e);
        }

    }

    private void process() {
        List<Transaction> orphanTxList = pool.getAllOrphan();
        if (orphanTxList.isEmpty()) {
            return;
        }
        Collections.sort(orphanTxList, TransactionTimeComparator.getInstance());
//        List<Transaction> txList = pool.getAll();
        Map<String, Coin> temporaryToMap = new HashMap<>();
        Set<String> temporaryFromSet = new HashSet<>();

//        for (Transaction tx : txList) {
//            if (null == tx.getCoinData() || null == tx.getCoinData().getTo()) {
//                continue;
//            }
//            for (Coin coin : tx.getCoinData().getTo()) {
//                temporaryToMap.put(Base64.getEncoder().encodeToString(coin.getOwner()), coin);
//            }
//        }
        List<Transaction> list = new ArrayList<>();
        for (Transaction tx : orphanTxList) {
            ValidateResult result = ledgerService.verifyCoinData(tx, temporaryToMap, temporaryFromSet);
            if (result.isSuccess()) {
                list.add(tx);
                pool.removeOrphan(tx.getHash());
            } else if (!result.getErrorCode().equals(TransactionErrorCode.ORPHAN_TX)) {
                pool.removeOrphan(tx.getHash());
                System.out.println("删除孤儿交易1:::::" + result.getMsg());
            } else if (tx.getTime() <= (TimeService.currentTimeMillis() - 600000L)) {
                pool.removeOrphan(tx.getHash());
                System.out.println("删除孤儿交易2:::::" + result.getMsg());
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            pool.addInFirst(new TxContainer(list.get(i)), false);
        }
    }
}
