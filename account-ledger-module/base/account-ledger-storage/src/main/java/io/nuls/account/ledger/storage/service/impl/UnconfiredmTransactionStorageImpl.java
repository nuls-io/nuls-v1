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
package io.nuls.account.ledger.storage.service.impl;

import io.nuls.account.ledger.storage.constant.AccountLedgerStorageConstant;
import io.nuls.account.ledger.storage.service.UnconfirmedTransactionStorageService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
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
import io.nuls.kernel.utils.TransactionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * author Facjas
 * date 2018/5/22.
 */
@Component
public class UnconfiredmTransactionStorageImpl implements UnconfirmedTransactionStorageService,InitializingBean {

    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = dbService.createArea(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX);
        if (result.isFailed()) {
            //TODO
        }
    }

    @Override
    public Result saveUnconfirmedTx(NulsDigestData hash,Transaction tx) {
        Result result;
        try {
            result = dbService.put(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX, hash.serialize(), tx.serialize());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        return result;
    }

    @Override
    public Result deleteUnconfirmedTx(NulsDigestData hash){
        try {
            return dbService.delete(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX, hash.serialize());
        }catch (Exception e){
            Log.info("deleteUnconfirmedTx error");
            return Result.getFailed();
        }
    }

    @Override
    public Result<Transaction> getUnconfirmedTx(NulsDigestData hash){
        try{
            byte[] txBytes = dbService.get(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX, hash.serialize());
            if(txBytes == null) {
                return Result.getSuccess();
            }
            Transaction tx = TransactionManager.getInstances(new NulsByteBuffer(txBytes),1).get(0);
            return Result.getSuccess().setData(tx);
        }catch (Exception e){
            return Result.getFailed();
        }
    }

    @Override
    public Result<List<Transaction>> loadAllUnconfirmedList() {
        Result result;
        List<Transaction> tmpList = new ArrayList<>();
        List<Entry<byte[], byte[]>> txs = dbService.entryList(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_TX);

        for (Entry txEntry : txs) {
            Transaction tmpTx = null;
            try {
                tmpTx = TransactionManager.getInstance(new NulsByteBuffer((byte[])txEntry.getValue()));
            } catch (Exception e) {
                Log.info("Load local transaction Error,transaction key[" + Hex.encode((byte[])txEntry.getKey()) + "]");
            }
            if (tmpTx != null) {
                tmpList.add(tmpTx);
            }
        }
        return Result.getSuccess().setData(tmpList);
    }
}
