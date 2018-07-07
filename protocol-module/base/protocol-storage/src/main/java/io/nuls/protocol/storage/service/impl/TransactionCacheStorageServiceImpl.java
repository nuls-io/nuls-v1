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
package io.nuls.protocol.storage.service.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.storage.service.TransactionCacheStorageService;

import java.io.IOException;

@Service
public class TransactionCacheStorageServiceImpl implements TransactionCacheStorageService, InitializingBean {

    private final static String TRANSACTION_CACHE_KEY_NAME = "transaction_cache";

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = this.dbService.createArea(TRANSACTION_CACHE_KEY_NAME);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public boolean putTx(Transaction tx) {
        if (tx == null) {
            return false;
        }
        byte[] txHashBytes = null;
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return false;
        }
        // 保存交易
        Result result = dbService.putModel(TRANSACTION_CACHE_KEY_NAME, txHashBytes, tx);
        return result.isSuccess();
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        byte[] hashBytes = null;
        try {
            hashBytes = hash.serialize();
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        Transaction tx = dbService.getModel(TRANSACTION_CACHE_KEY_NAME, hashBytes, Transaction.class);
        return tx;
    }

    @Override
    public boolean removeTx(NulsDigestData hash) {
        if(hash == null) {
            return false;
        }
        try {
            Result result = dbService.delete(TRANSACTION_CACHE_KEY_NAME, hash.serialize());
            return result.isSuccess();
        } catch (IOException e) {
            Log.error(e);
        }
        return false;
    }
}
