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
 */
package io.nuls.consensus.poc.block.validator;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class BlockTxValidator implements NulsDataValidator<Block> {
    private static final String ERROR_MESSAGE = "";
    public static final BlockTxValidator INSTANCE = new BlockTxValidator();

    private BlockTxValidator() {
    }

    public static BlockTxValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Block block) {
        if (block.getHeader().getTxCount() != block.getTxs().size()) {
            return ValidateResult.getFailedResult("txCount is wrong!");
        }
        int count = 0;
        List<Transaction> txList = new ArrayList<>();
        for (Transaction tx : block.getTxs()) {
            ValidateResult result = tx.verify();
            if(result.isFailed()&&result.getErrorCode()== ErrorCode.ORPHAN_TX){
                AbstractCoinTransaction coinTx = (AbstractCoinTransaction) tx;
                result = coinTx.getCoinDataProvider().verifyCoinData(coinTx,txList);
                if(result.isSuccess()){
                    coinTx.setSkipInputValidator(true);
                    result = coinTx.verify();
                    coinTx.setSkipInputValidator(false);
                }
            }
            if (null==result||result.isFailed()) {
                if(result.getErrorCode()== ErrorCode.ORPHAN_TX){
                    return result;
                }
                return ValidateResult.getFailedResult("there is wrong transaction!msg:"+result.getMessage());
            }
            if (tx.getType() == TransactionConstant.TX_TYPE_COIN_BASE) {
                count++;
            }
            txList.add(tx);
        }
        if (count > 1) {
            return ValidateResult.getFailedResult("coinbase transaction must only one!");
        }
        return ValidateResult.getSuccessResult();
    }
}
