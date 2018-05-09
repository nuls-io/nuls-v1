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

import io.nuls.db.service.DBService;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.Transaction;
import io.nuls.ledger.storage.constant.LedgerStorageConstant;
import io.nuls.ledger.storage.service.UtxoLedgerStorageService;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/5/8
 */
@Service
public class UtxoLedgerStorageServiceImpl implements UtxoLedgerStorageService {

    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    public UtxoLedgerStorageServiceImpl() {
        Result result = dbService.createArea(LedgerStorageConstant.DB_AREA_LEDGER_TRANSACTION);
        if(result.isFailed()) {
            //TODO 处理创建area失败的逻辑
        }
        result = dbService.createArea(LedgerStorageConstant.DB_AREA_LEDGER_UTXO);
        if(result.isFailed()) {
            //TODO 处理创建area失败的逻辑
        }

    }

    @Override
    public Result saveTx(Transaction tx) {
        if(tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] hashBytes = tx.getHash().serialize();
        Result result = dbService.putModel(LedgerStorageConstant.DB_AREA_LEDGER_TRANSACTION, hashBytes, tx);
        return result;
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        if(hash == null) {
            return null;
        }
        byte[] hashBytes = hash.serialize();
        Transaction tx = dbService.getModel(LedgerStorageConstant.DB_AREA_LEDGER_TRANSACTION, hashBytes, Transaction.class);
        return tx;
    }

    @Override
    public Result deleteTx(Transaction tx) {
        if(tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] hashBytes = tx.getHash().serialize();
        Result result = dbService.delete(LedgerStorageConstant.DB_AREA_LEDGER_TRANSACTION, hashBytes);
        return result;
    }

    @Override
    public Result saveUtxo(Coin coin) {
        if(coin == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        Result result = dbService.put(LedgerStorageConstant.DB_AREA_LEDGER_UTXO, coin.getOwner(), coin.serialize());
        return result;
    }

    @Override
    public Result deleteUtxo(Coin coin) {
        if(coin == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        Result result = dbService.delete(LedgerStorageConstant.DB_AREA_LEDGER_UTXO, coin.getOwner());
        return result;
    }
}
