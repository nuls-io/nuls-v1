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
package io.nuls.consensus.poc.storage.service.impl;

import io.nuls.consensus.poc.storage.service.TransactionCacheStorageService;
import io.nuls.core.tools.crypto.Util;
import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
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

@Component
public class TransactionCacheStorageServiceImpl implements TransactionCacheStorageService, InitializingBean {

    private final static String TRANSACTION_CACHE_KEY_NAME = "transactions_cache";
    private final static byte[] LAST_KEY = "last_key".getBytes();
    private final static byte[] START_KEY = "start_key".getBytes();
    private int lastIndex = 0;
    private int startIndex = 0;

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Override
    public void afterPropertiesSet() throws NulsException {
        dbService.destroyArea(TRANSACTION_CACHE_KEY_NAME);

        Result result = this.dbService.createArea(TRANSACTION_CACHE_KEY_NAME);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
        startIndex = 1;
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
        Result result = null;
        try {
            result = dbService.put(TRANSACTION_CACHE_KEY_NAME, txHashBytes, tx.serialize());
        } catch (IOException e) {
            Log.error(e);
            return false;
        }

//        if(!result.isSuccess()) {
//            return result.isSuccess();
//        }
//        lastIndex++;
//        byte[] lastIndexBytes = Util.intToBytes(lastIndex);
//        result = dbService.put(TRANSACTION_CACHE_KEY_NAME, lastIndexBytes, txHashBytes);
//        if(!result.isSuccess()) {
//            removeTx(tx.getHash());
//            return result.isSuccess();
//        }
//        result = dbService.put(TRANSACTION_CACHE_KEY_NAME, LAST_KEY, lastIndexBytes);
        return result.isSuccess();
    }

    @Override
    public int getStartIndex() {
        byte[] lastIndexBytes = dbService.get(TRANSACTION_CACHE_KEY_NAME, START_KEY);
        if (lastIndexBytes == null) {
            return 0;
        }
        return Util.byteToInt(lastIndexBytes);
    }

    @Override
    public Transaction pollTx() {

        byte[] startIndexBytes = Util.intToBytes(startIndex);

        byte[] hashBytes = dbService.get(TRANSACTION_CACHE_KEY_NAME, startIndexBytes);
        if (hashBytes == null) {
            return null;
        }

        byte[] txBytes = dbService.get(TRANSACTION_CACHE_KEY_NAME, hashBytes);
        Transaction tx = null;
        if (null != txBytes) {
            try {
                tx = TransactionManager.getInstance(new NulsByteBuffer(txBytes, 0));
            } catch (Exception e) {
                Log.error(e);
                return null;
            }
        }

        startIndex++;
//        dbService.put(TRANSACTION_CACHE_KEY_NAME, START_KEY, Util.intToBytes(startIndex));

        return tx;
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
        byte[] txBytes = dbService.get(TRANSACTION_CACHE_KEY_NAME, hashBytes);
        Transaction tx = null;
        if (null != txBytes) {
            try {
                tx = TransactionManager.getInstance(new NulsByteBuffer(txBytes, 0));
            } catch (Exception e) {
                Log.error(e);
                return null;
            }
        }
        return tx;
    }

    @Override
    public boolean removeTx(NulsDigestData hash) {
        if (hash == null) {
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
