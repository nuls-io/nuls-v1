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
package io.nuls.ledger.service.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.db.service.BatchOperation;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.utils.VarInt;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.storage.service.UtxoLedgerUtxoStorageService;
import io.nuls.ledger.storage.service.UtxoLedgerTransactionStorageService;
import io.nuls.ledger.utils.LedgerUtil;
import org.spongycastle.util.Arrays;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.nuls.core.tools.str.StringUtils.asString;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/8
 */
@Service
public class UtxoLedgerServiceImpl implements LedgerService {

    private final static String CLASS_NAME = UtxoLedgerServiceImpl.class.getName();

    @Autowired
    private UtxoLedgerUtxoStorageService utxoLedgerUtxoStorageService;
    @Autowired
    private UtxoLedgerTransactionStorageService utxoLedgerTransactionStorageService;

    @Override
    public Result saveTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER);
        }
        try {
            // 保存CoinData
            Result result = saveCoinData(tx);
            if(result.isFailed()) {
                //TODO pierre 是否由内部调用回滚
                rollbackCoinData(tx);
                return result;
            }
            // 保存交易
            result = utxoLedgerTransactionStorageService.saveTx(tx);
            if(result.isFailed()) {
                //TODO pierre 是否由内部调用回滚
                rollbackTx(tx);
            }
            return result;
        } catch (Exception e) {
            //TODO pierre 是否由内部调用回滚
            rollbackTx(tx);
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    private Result saveCoinData(Transaction tx) throws IOException {
        byte[] txHashBytes = txHashBytes = tx.getHash().serialize();
        BatchOperation batch = utxoLedgerUtxoStorageService.createWriteBatch();
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // 删除utxo已花费 - from
            List<Coin> froms = coinData.getFrom();
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
                    return Result.getFailed(e.getMessage());
                }
            }
            // 执行批量
            Result batchResult = batch.executeBatch();
            if (batchResult.isFailed()) {
                return batchResult;
            }
        }
        return Result.getSuccess();
    }

    @Override
    public Result rollbackTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER);
        }
        try {
            // 回滚CoinData
            Result result = rollbackCoinData(tx);
            if(result.isFailed()) {
                return result;
            }
            // 回滚交易
            result = utxoLedgerTransactionStorageService.deleteTx(tx);
            return result;
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }

    private Result rollbackCoinData(Transaction tx) throws IOException {
        byte[] txHashBytes = txHashBytes = tx.getHash().serialize();
        BatchOperation batch = utxoLedgerUtxoStorageService.createWriteBatch();
        CoinData coinData = tx.getCoinData();
        if (coinData != null) {
            // 保存utxo已花费 - from
            List<Coin> froms = coinData.getFrom();
            for (Coin from : froms) {
                try {
                    batch.put(from.getOwner(), from.serialize());
                } catch (IOException e) {
                    Log.error(e);
                    return Result.getFailed(e.getMessage());
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
        return Result.getSuccess();
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        return utxoLedgerTransactionStorageService.getTx(hash);
    }

    @Override
    public ValidateResult verifyCoinData(CoinData coinData) {
        if (coinData == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        List<Coin> froms = coinData.getFrom();
        HashSet set = new HashSet(froms.size());
        Na fromTotal = Na.ZERO;
        byte[] fromBytes;
        for (Coin from : froms) {
            fromBytes = from.getOwner();
            // 验证双花
            if(!set.add(asString(fromBytes))) {
                return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.LEDGER_DOUBLE_SPENT);
            }
            // 验证是否可用，检查是否还在锁定时间内
            if (TimeService.currentTimeMillis() < from.getLockTime()) {
                return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_UNUSABLE);
            }
            // 验证是否可花费, 检查数据中是否存在此UTXO
            if(null == utxoLedgerUtxoStorageService.getUtxoBytes(fromBytes)) {
                return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_NOT_FOUND);
            }
            fromTotal = fromTotal.add(from.getNa());
        }
        List<Coin> tos = coinData.getTo();
        Na toTotal = Na.ZERO;
        for (Coin to : tos) {
            toTotal = toTotal.add(to.getNa());
        }
        // 验证输出不能大于输入
        if (fromTotal.compareTo(toTotal) < 0) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.INVALID_AMOUNT);
        }
        return ValidateResult.getSuccessResult();
    }

    @Override
    public ValidateResult verifyCoinData(CoinData coinData, List<Transaction> txList) {
        if (coinData == null || txList == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        try {
            int initialCapacity = 0;
            CoinData validateCoinData;
            List<Coin> validateTos;
            // txList中所有的from.getOwner()存放于HashSet中
            // 计算HashSet容量
            for(Transaction tx : txList) {
                validateCoinData = tx.getCoinData();
                if(validateCoinData == null) {
                    continue;
                }
                // 此txList是待保存交易列表，等待打包的交易，所以toList是下一步的UTXO，应该校验它
                //TODO pierre 此处txList是什么业务逻辑
                initialCapacity += tx.getCoinData().getTo().size();
            }
            Set<String> validateUtxoKeySet = new HashSet<>(initialCapacity);
            Transaction tx;
            for(int i = 0, length = txList.size(); i < length; i++) {
                tx = txList.get(i);
                validateCoinData = tx.getCoinData();
                if(validateCoinData == null) {
                    continue;
                }
                // 此txList是待保存交易列表，等待打包的交易，所以toList是下一步的UTXO，应该校验它
                //TODO pierre 此处txList是什么业务逻辑
                validateTos = validateCoinData.getTo();
                for(Coin to : validateTos) {
                    validateUtxoKeySet.add(asString(Arrays.concatenate(tx.getHash().serialize(), new VarInt(i).encode())));
                }
            }

            // 遍历需校验的coinData的fromOwner，如果既不存在于txList，又不存在于数据库中，那么这是一笔问题数据，进一步查是否存在这笔交易，交易有就是双花，没有就是孤儿交易，则返回失败
            List<Coin> froms = coinData.getFrom();
            byte[] fromBytes;
            for(Coin from : froms) {
                fromBytes = from.getOwner();
                if(validateUtxoKeySet.contains(asString(fromBytes))) {
                    continue;
                } else {
                    if(null == utxoLedgerUtxoStorageService.getUtxoBytes(fromBytes)) {
                        if(null != utxoLedgerTransactionStorageService.getTxBytes(LedgerUtil.getTxHashBytes(fromBytes))) {
                            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.LEDGER_DOUBLE_SPENT);
                        } else {
                            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.ORPHAN_TX);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.error(e);
            return ValidateResult.getFailedResult(CLASS_NAME, e.getMessage());
        }
        return ValidateResult.getSuccessResult();
    }

    @Override
    public ValidateResult verifyDoubleSpend(Block block) {
        if (block == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        return verifyDoubleSpend(block.getTxs());
    }

    @Override
    public ValidateResult verifyDoubleSpend(List<Transaction> txList) {
        if (txList == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        // 计算HashSet容量
        int initialCapacity = 0;
        CoinData coinData;
        for(Transaction tx : txList) {
            coinData = tx.getCoinData();
            if(coinData == null) {
                continue;
            }
            initialCapacity += tx.getCoinData().getFrom().size();
        }
        HashSet<String> fromSet = new HashSet<>(initialCapacity);
        List<Coin> froms;
        // 判断是否有重复的fromCoin存在，如果存在，则是双花
        for(Transaction tx : txList) {
            coinData = tx.getCoinData();
            if(coinData == null) {
                continue;
            }
            froms = coinData.getFrom();
            for(Coin from : froms) {
                if(!fromSet.add(asString(from.getOwner()))) {
                    return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.LEDGER_DOUBLE_SPENT);
                }
            }
        }
        return ValidateResult.getSuccessResult();
    }

    @Override
    public Result unlockTxCoinData(Transaction tx) {
        if (tx == null || tx.getCoinData() == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        try {
            CoinData coinData = tx.getCoinData();
            List<Coin> froms = coinData.getFrom();
            for(Coin from : froms) {
                if(from.getLockTime() != -1) {
                    return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_STATUS_CHANGE);
                }
            }
            Result result = saveCoinData(tx);
            if(result.isFailed()) {
                //TODO pierre 是否由内部调用回滚
                rollbackCoinData(tx);
            }
            return result;
        } catch (IOException e) {
            //TODO pierre 是否由内部调用回滚
            try {
                rollbackCoinData(tx);
            } catch (IOException e1) {
                //skip it
            }
            Log.error(e);
            return Result.getFailed(e.getMessage());
        }
    }
}
