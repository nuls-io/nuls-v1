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
import io.nuls.account.ledger.storage.service.LocalUtxoStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.model.Entry;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Result;
import io.nuls.ledger.service.LedgerService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Facjas
 * @date 2018/5/10.
 */
@Component
public class LocalUtxoStorageServiceImpl implements LocalUtxoStorageService, InitializingBean {
    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    @Autowired
    private LedgerService ledgerService;

    private Map<String, Entry<byte[], byte[]>> cacheMap;

    @Override
    public void afterPropertiesSet() throws NulsException {

        Result result = dbService.createArea(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public Collection<Entry<byte[], byte[]>> loadAllCoinList() {
        if(cacheMap == null) {
            cacheMap = new ConcurrentHashMap<>();

            List<Entry<byte[], byte[]>> coinList = dbService.entryList(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA);
            for(Entry<byte[], byte[]> entry : coinList) {
                cacheMap.put(new String(entry.getKey()), entry);
            }
        }
        return cacheMap.values();
    }

    @Override
    public Result saveUTXO(byte[] key, byte[] value) {
        Result result = dbService.put(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA, key, value);

        if(result.isSuccess() && cacheMap != null) {
            cacheMap.put(new String(key), new Entry(key, value));
        }

        return result;
    }

    @Override
    public Result<Integer> batchSaveUTXO(Map<byte[], byte[]> utxos) {
        BatchOperation batch = dbService.createWriteBatch(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA);
        Set<Map.Entry<byte[], byte[]>> utxosToSaveEntries = utxos.entrySet();
        for(Map.Entry<byte[], byte[]> entry : utxosToSaveEntries) {
            batch.put(entry.getKey(), entry.getValue());
        }
        Result batchResult = batch.executeBatch();
        if (batchResult.isFailed()) {
            return batchResult;
        }
        Result result = Result.getSuccess().setData(new Integer(utxos.size()));

        if(result.isSuccess() && cacheMap != null) {
            for(Map.Entry<byte[], byte[]> entry : utxosToSaveEntries) {
                cacheMap.put(new String(entry.getKey()), new Entry(entry.getKey(), entry.getValue()));
            }
        }

        return result;
    }

    @Override
    public Result deleteUTXO(byte[] key) {
        Result result = dbService.delete(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA, key);
        if(result.isSuccess() && cacheMap != null) {
            cacheMap.remove(new String(key));
        }
        return result;
    }

    @Override
    public Result batchDeleteUTXO(Set<byte[]> utxos) {
        BatchOperation batch = dbService.createWriteBatch(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA);
        for (byte[] key : utxos) {
            batch.delete(key);
        }
        Result batchResult = batch.executeBatch();
        if (batchResult.isFailed()) {
            return batchResult;
        }
        Result result = Result.getSuccess().setData(new Integer(utxos.size()));

        if(result.isSuccess() && cacheMap != null) {
            for (byte[] key : utxos) {
                cacheMap.remove(new String(key));
            }
        }
        return result;
    }

    @Override
    public Result batchSaveAndDeleteUTXO(List<Entry<byte[], byte[]>> utxosToSave, List<byte[]> utxosToDelete) {
        BatchOperation batch = dbService.createWriteBatch(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA);
        for (byte[] key : utxosToDelete) {
            batch.delete(key);
        }
        for(Entry<byte[], byte[]> entry : utxosToSave) {
            batch.put(entry.getKey(), entry.getValue());
        }
        Result batchResult = batch.executeBatch();
        if (batchResult.isFailed()) {
            return batchResult;
        }
        Result result = Result.getSuccess().setData(new Integer(utxosToSave.size() + utxosToDelete.size()));

        if(result.isSuccess() && cacheMap != null) {
            for(Entry<byte[], byte[]> entry : utxosToSave) {
                cacheMap.put(new String(entry.getKey()), entry);
            }
            for (byte[] key : utxosToDelete) {
                cacheMap.remove(new String(key));
            }
        }

        return result;
    }

    @Override
    public byte[] getUtxoBytes(byte[] owner) {
        if (owner == null) {
            return null;
        }
        return dbService.get(AccountLedgerStorageConstant.DB_NAME_ACCOUNT_LEDGER_COINDATA, owner);
    }

    @Override
    public Coin getUtxo(byte[] owner) {
        byte[] utxoBytes = getUtxoBytes(owner);
        Coin coin = null;
        try {
            if(utxoBytes != null) {
                coin = new Coin();
                coin.parse(utxoBytes, 0);
            }
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
        return coin;
    }
}
