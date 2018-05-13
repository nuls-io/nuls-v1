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

import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.storage.service.UtxoLedgerStorageService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.nuls.core.tools.str.StringUtils.asString;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/5/8
 */
@Service
public class UtxoLedgerServiceImpl implements LedgerService {

    private final static String CLASS_NAME = UtxoLedgerServiceImpl.class.getName();
    private final static int TX_HASH_LENGTH = 35;

    @Autowired
    private UtxoLedgerStorageService storageService;

    @Override
    public Result saveTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER);
        }
        // 保存交易
        Result result = storageService.saveTx(tx);
        return result;
    }

    @Override
    public Result rollbackTx(Transaction tx) {
        if (tx == null) {
            return Result.getFailed(LedgerErrorCode.NULL_PARAMETER);
        }
        Result result = storageService.deleteTx(tx);
        return result;
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        return storageService.getTx(hash);
    }

    @Override
    public ValidateResult verifyCoinData(CoinData coinData) {
        if (coinData == null) {
            return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.NULL_PARAMETER);
        }
        List<Coin> froms = coinData.getFrom();
        Na fromTotal = Na.ZERO;
        for (Coin from : froms) {
            if (TimeService.currentTimeMillis() < from.getLockTime()) {
                return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_UNUSABLE);
            }
            fromTotal = fromTotal.add(from.getNa());
        }
        List<Coin> tos = coinData.getTo();
        Na toTotal = Na.ZERO;
        for (Coin to : tos) {
            toTotal = toTotal.add(to.getNa());
        }
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
        int initialCapacity = 0;
        CoinData validateCoinData;
        List<Coin> validateFroms;
        // txList中所有的from.getOwner()存放于HashSet中
        // 计算HashSet容量
        for(Transaction tx : txList) {
            validateCoinData = tx.getCoinData();
            if(validateCoinData == null) {
                continue;
            }
            initialCapacity += tx.getCoinData().getFrom().size();
        }
        Set<String> validateUtxoKeySet = new HashSet<>(initialCapacity);
        for(Transaction tx : txList) {
            validateCoinData = tx.getCoinData();
            if(validateCoinData == null) {
                continue;
            }
            validateFroms = validateCoinData.getFrom();
            for(Coin from : validateFroms) {
                validateUtxoKeySet.add(asString(from.getOwner()));
            }
        }

        // 遍历需校验的coinData的fromOwner，如果既不存在于txList，又不存在于数据库中，则查是否存在这笔交易，交易有就是双花，没有就是孤儿交易，则返回失败
        List<Coin> froms = coinData.getFrom();
        byte[] fromBytes;
        for(Coin from : froms) {
            fromBytes = from.getOwner();
            if(validateUtxoKeySet.contains(asString(fromBytes))) {
                continue;
            } else {
                if(null == storageService.getCoinBytes(fromBytes)) {
                    if(null != storageService.getTxBytes(getTxBytes(fromBytes))) {
                        return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.LEDGER_DOUBLE_SPENT);
                    } else {
                        return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.ORPHAN_TX);
                    }
                }
            }
        }
        return ValidateResult.getSuccessResult();
    }

    private byte[] getTxBytes(byte[] fromBytes) {
        if(fromBytes == null || fromBytes.length < TX_HASH_LENGTH) {
            return null;
        }
        byte[] txBytes = new byte[TX_HASH_LENGTH];
        System.arraycopy(fromBytes, 0, txBytes, 0, TX_HASH_LENGTH);
        return txBytes;
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
        CoinData coinData = tx.getCoinData();
        List<Coin> froms = coinData.getFrom();
        for(Coin from : froms) {
            if(from.getLockTime() != -1) {
                return ValidateResult.getFailedResult(CLASS_NAME, LedgerErrorCode.UTXO_UNUSABLE);
            }
        }
        Result result = storageService.saveTx(tx);
        return result;
    }
}
