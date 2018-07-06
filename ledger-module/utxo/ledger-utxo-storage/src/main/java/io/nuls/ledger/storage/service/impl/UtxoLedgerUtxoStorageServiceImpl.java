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
package io.nuls.ledger.storage.service.impl;

import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.model.Entry;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Result;
import io.nuls.ledger.storage.constant.LedgerStorageConstant;
import io.nuls.ledger.storage.service.UtxoLedgerUtxoStorageService;

import java.io.IOException;
import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 */
@Service
public class UtxoLedgerUtxoStorageServiceImpl implements UtxoLedgerUtxoStorageService, InitializingBean {

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() throws NulsException {
        Result result = dbService.createArea(LedgerStorageConstant.DB_NAME_LEDGER_UTXO);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
    }

    @Override
    public BatchOperation createWriteBatch() {
        return dbService.createWriteBatch(LedgerStorageConstant.DB_NAME_LEDGER_UTXO);
    }

    @Override
    public Result saveUtxo(byte[] owner, Coin coin) {
        try {
            Log.info("save utxo::" + Hex.encode(owner));
            return dbService.put(LedgerStorageConstant.DB_NAME_LEDGER_UTXO, owner, coin.serialize());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(KernelErrorCode.IO_ERROR);
        }
    }

    @Override
    public Coin getUtxo(byte[] owner) {
        byte[] utxoBytes = getUtxoBytes(owner);
        Coin coin = null;
        try {
            if (utxoBytes != null) {
                coin = new Coin();
                coin.parse(utxoBytes, 0);
            }
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
        return coin;
    }

    @Override
    public Result deleteUtxo(byte[] owner) {
        return dbService.delete(LedgerStorageConstant.DB_NAME_LEDGER_UTXO, owner);
    }

    @Override
    public byte[] getUtxoBytes(byte[] owner) {
        if (owner == null) {
            return null;
        }
        return dbService.get(LedgerStorageConstant.DB_NAME_LEDGER_UTXO, owner);
    }

    @Override
    public List<byte[]> getAllUtxoBytes() {
        return dbService.valueList(LedgerStorageConstant.DB_NAME_LEDGER_UTXO);
    }

    @Override
    public List<Entry<byte[], byte[]>> getAllUtxoEntryBytes() {
        return dbService.entryList(LedgerStorageConstant.DB_NAME_LEDGER_UTXO);
    }

}
