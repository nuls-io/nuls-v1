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
package io.nuls.accountLedger.storage.service.impl;

import io.nuls.accountLedger.storage.constant.AccountLedgerStorageConstant;
import io.nuls.accountLedger.storage.po.TransactionInfoPo;
import io.nuls.accountLedger.storage.service.AccountLedgerStorageService;
import io.nuls.core.tools.log.Log;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.DBService;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.VarInt;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.util.List;

/**
 * author Facjas
 * date 2018/5/10.
 */
@Component
public class AccountLedgerStorageServiceImpl implements AccountLedgerStorageService {
    /**
     * 通用数据存储服务
     * Universal data storage services.
     */
    @Autowired
    private DBService dbService;

    public AccountLedgerStorageServiceImpl() {

        Result result = dbService.createArea(AccountLedgerStorageConstant.DB_AREA_ACCOUNTLEDGER_TRANSACTION);
        if (result.isFailed()) {
            //TODO
        }

        result = dbService.createArea(AccountLedgerStorageConstant.DB_AREA_ACCOUNTLEDGER_COINDATA);
        if (result.isFailed()) {
            //TODO
        }

        result = dbService.createArea(AccountLedgerStorageConstant.DB_AREA_ACCOUNTLEDGER_TXINFO);
        if (result.isFailed()) {
            //TODO
        }
    }

    @Override
    public Result saveLocalTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        byte[] txHashBytes = new byte[0];
        try {
            txHashBytes = tx.getHash().serialize();
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // delete - from
            List<Coin> froms = coinData.getFrom();
            BatchOperation batch = dbService.createWriteBatch(AccountLedgerStorageConstant.DB_AREA_ACCOUNTLEDGER_COINDATA);
            for (Coin from : froms) {
                batch.delete(from.getOwner());
            }
            // save utxo - to
            List<Coin> tos = coinData.getTo();
            byte[] indexBytes;
            for (int i = 0, length = tos.size(); i < length; i++) {
                try {
                    batch.put(Arrays.concatenate(txHashBytes, new VarInt(i).encode()), tos.get(i).serialize());
                } catch (IOException e) {
                    throw new NulsRuntimeException(e);
                }
            }

            Result batchResult = batch.executeBatch();
            if (batchResult.isFailed()) {
                return batchResult;
            }
        }

        Result result = null;
        try {
            result = dbService.put(AccountLedgerStorageConstant.DB_AREA_ACCOUNTLEDGER_TRANSACTION, tx.getHash().serialize(), tx.serialize());
        } catch (IOException e) {
            throw new NulsRuntimeException(e);
        }
        return result;
    }

    @Override
    public Result saveLocalTxInfo(TransactionInfoPo tx) {
        if (tx == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        return null;
    }

    @Override
    public Result deleteLocalTxInfo(TransactionInfoPo tx) {
        return null;
    }

    @Override
    public Transaction getLocalTx(NulsDigestData hash) {
        return null;
    }

    @Override
    public Result deleteLocalTx(Transaction tx) {
        return null;
    }

    @Override
    public byte[] getCoinBytes(byte[] owner) {
        return new byte[0];
    }

    @Override
    public byte[] getTxBytes(byte[] txBytes) {
        return new byte[0];
    }
}
