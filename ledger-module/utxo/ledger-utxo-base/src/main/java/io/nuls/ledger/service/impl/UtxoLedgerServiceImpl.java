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

import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.TransactionErrorCode;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.*;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.ledger.service.LedgerService;
import io.nuls.ledger.storage.service.UtxoLedgerStorageService;

import java.text.BreakIterator;
import java.util.List;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/5/8
 */
@Service
public class UtxoLedgerServiceImpl implements LedgerService {

    @Autowired
    private UtxoLedgerStorageService storageService;

    @Override
    public Result saveTx(Transaction tx) {
        if(tx == null || tx.getCoinData() == null) {
            return Result.getFailed(KernelErrorCode.NULL_PARAMETER);
        }
        // 保存coindata
        CoinData coinData = tx.getCoinData();
        List<Coin> froms = coinData.getFrom();
        for(Coin from : froms) {

        }

        List<Coin> tos = coinData.getTo();
        for(Coin to : tos) {

        }
        // 保存交易
        Result result = storageService.saveTx(tx);
        if(result.isFailed()){
            return result;
        }
        return Result.getSuccess();
    }

    @Override
    public Result rollbackTx(Transaction tx) {
        return null;
    }

    @Override
    public Transaction getTx(NulsDigestData hash) {
        return storageService.getTx(hash);
    }

    @Override
    public ValidateResult verifyCoinData(CoinData coinData) {
        List<Coin> froms = coinData.getFrom();
        long fromTotal = 0L;
        for (Coin from : froms) {
            if(TimeService.currentTimeMillis() < from.getLockTime()) {
                return ValidateResult.getFailedResult(UtxoLedgerServiceImpl.class.getName(), TransactionErrorCode.UTXO_UNUSABLE);
            }
            fromTotal += from.getNa().getValue();
        }
        List<Coin> tos = coinData.getTo();
        long toTotal = 0L;
        for (Coin to : tos) {
            toTotal += to.getNa().getValue();
        }
        if(fromTotal != toTotal) {
            return ValidateResult.getFailedResult(UtxoLedgerServiceImpl.class.getName(), TransactionErrorCode.INVALID_INPUT);
        }
        return ValidateResult.getSuccessResult();
    }

    @Override
    public ValidateResult verifyCoinData(CoinData coinData, List<Transaction> txList) {
        return null;
    }

    @Override
    public ValidateResult verifyDoubleSpend(Block block) {
        return null;
    }

    @Override
    public ValidateResult verifyDoubleSpend(List<Transaction> txList) {
        return null;
    }

    @Override
    public Result unlockTxCoinData(Transaction tx) {
        return null;
    }
}
