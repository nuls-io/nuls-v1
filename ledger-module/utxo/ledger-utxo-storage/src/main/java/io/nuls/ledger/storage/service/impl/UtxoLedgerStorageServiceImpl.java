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

import io.nuls.core.tools.log.Log;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.VarInt;
import io.nuls.ledger.storage.constant.LedgerStorageConstant;
import io.nuls.ledger.storage.service.UtxoLedgerStorageService;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.util.List;

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
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }
        result = dbService.createArea(LedgerStorageConstant.DB_AREA_LEDGER_UTXO);
        if (result.isFailed() && !DBErrorCode.DB_AREA_EXIST.equals(result.getErrorCode())) {
            throw new NulsRuntimeException(result.getErrorCode());
        }

    }

    @Override
    public Result saveTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] txHashBytes = new byte[0];
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            Result.getFailed(e.getMessage());
        }
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // 删除utxo已花费 - from
            List<Coin> froms = coinData.getFrom();
            BatchOperation batch = dbService.createWriteBatch(LedgerStorageConstant.DB_AREA_LEDGER_UTXO);
            for (Coin from : froms) {
                batch.delete(from.getOwner());
            }
            // 保存utxo - to
            List<Coin> tos = coinData.getTo();
            byte[] indexBytes;
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    batch.put(Arrays.concatenate(txHashBytes, new VarInt(i).encode()), tos.get(i).serialize());
                } catch (IOException e) {
                    Log.error(e);
                    Result.getFailed(e.getMessage());
                }
            }
            // 执行批量
            Result batchResult = batch.executeBatch();
            if (batchResult.isFailed()) {
                return batchResult;
            }
        }
        // 保存交易
        Result result = dbService.putModel(LedgerStorageConstant.DB_AREA_LEDGER_TRANSACTION, txHashBytes, tx);
        return result;
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        if (hash == null) {
            return null;
        }
        byte[] hashBytes = new byte[0];
        try {
            hashBytes = hash.serialize();
        } catch (IOException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        Transaction tx = dbService.getModel(LedgerStorageConstant.DB_AREA_LEDGER_TRANSACTION, hashBytes, Transaction.class);
        return tx;
    }

    @Override
    public Result deleteTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] txHashBytes = new byte[0];
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // 保存utxo已花费 - from
            List<Coin> froms = coinData.getFrom();
            BatchOperation batch = dbService.createWriteBatch(LedgerStorageConstant.DB_AREA_LEDGER_UTXO);
            for (Coin from : froms) {
                try {
                    batch.put(from.getOwner(), from.serialize());
                } catch (IOException e) {
                    Log.error(e);
                    Result.getFailed(e.getMessage());
                }
            }
            // 删除utxo - to
            List<Coin> tos = coinData.getTo();
            byte[] indexBytes;
            for (int i = 0, length = tos.size(); i < length; i++) {
                batch.delete(Arrays.concatenate(txHashBytes, new VarInt(i).encode()));
            }
            // 执行批量
            Result batchResult = batch.executeBatch();
            if (batchResult.isFailed()) {
                return batchResult;
            }
        }
        // 删除交易
        Result result = dbService.delete(LedgerStorageConstant.DB_AREA_LEDGER_TRANSACTION, txHashBytes);
        return result;
    }

    @Override
    public byte[] getCoinBytes(byte[] owner) {
        if (owner == null) {
            return null;
        }
        return dbService.get(LedgerStorageConstant.DB_AREA_LEDGER_UTXO, owner);
    }

    @Override
    public byte[] getTxBytes(byte[] txBytes) {
        if (txBytes == null) {
            return null;
        }
        return dbService.get(LedgerStorageConstant.DB_AREA_LEDGER_TRANSACTION, txBytes);
    }

}
