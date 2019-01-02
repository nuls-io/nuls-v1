/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.account.ledger.storage.service.impl;

import io.nuls.account.ledger.storage.constant.AccountLedgerStorageConstant;
import io.nuls.account.ledger.storage.po.UnconfirmedTxPo;
import io.nuls.account.ledger.storage.service.UnconfirmedTransactionStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.model.Entry;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.NulsByteBuffer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * author Facjas
 * date 2018/5/22.
 */
@Component
public class UnconfiredmTransactionStorageImpl implements UnconfirmedTransactionStorageService, InitializingBean {

    @Autowired
    private DBService dbService;

    private long sequence = System.currentTimeMillis();

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = dbService.createArea(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Result saveUnconfirmedTx(NulsDigestData hash, Transaction tx) {
        Result result;
        try {
            sequence++;
            UnconfirmedTxPo po = new UnconfirmedTxPo(tx, sequence);
            result = dbService.put(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX, hash.serialize(), po.serialize());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.getFailed();
        }
        return result;
    }

    @Override
    public Result deleteUnconfirmedTx(NulsDigestData hash) {
        try {
            return dbService.delete(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX, hash.serialize());
        } catch (Exception e) {
            Log.info("deleteUnconfirmedTx error");
            return Result.getFailed();
        }
    }

    @Override
    public Result<Transaction> getUnconfirmedTx(NulsDigestData hash) {
        try {
            byte[] txBytes = dbService.get(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX, hash.serialize());
            if (txBytes == null) {
                return Result.getSuccess();
            }
            UnconfirmedTxPo po = new UnconfirmedTxPo(txBytes);
            Transaction tx = po.getTx();
            return Result.getSuccess().setData(tx);
        } catch (Exception e) {
            return Result.getFailed();
        }
    }

    @Override
    public Result<List<Transaction>> loadAllUnconfirmedList() {
        Result result;
        List<UnconfirmedTxPo> tmpList = new ArrayList<>();
        List<Entry<byte[], byte[]>> txs = dbService.entryList(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX);

        for (Entry<byte[], byte[]> txEntry : txs) {
            try {
                UnconfirmedTxPo tmpTx = new UnconfirmedTxPo(txEntry.getValue());
                if (tmpTx != null) {
                    NulsByteBuffer buffer = new NulsByteBuffer(txEntry.getKey(), 0);
                    tmpTx.getTx().setHash(buffer.readHash());
                    tmpList.add(tmpTx);
                }
            } catch (Exception e) {
                Log.warn("parse local tx error", e);
            }
        }

        tmpList.sort(new Comparator<UnconfirmedTxPo>() {
            @Override
            public int compare(UnconfirmedTxPo o1, UnconfirmedTxPo o2) {
                return (int) (o1.getSequence() - o2.getSequence());
            }
        });

        List<Transaction> resultList = new ArrayList<>();
        for (UnconfirmedTxPo po : tmpList) {
            resultList.add(po.getTx());
        }

        return Result.getSuccess().setData(resultList);
    }
}
