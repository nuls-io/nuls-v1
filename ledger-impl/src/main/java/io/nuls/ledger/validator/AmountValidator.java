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
package io.nuls.ledger.validator;

import io.nuls.core.chain.entity.Na;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.entity.UtxoData;
import io.nuls.ledger.entity.UtxoOutput;
import io.nuls.ledger.entity.tx.AbstractCoinTransaction;
import io.nuls.ledger.entity.tx.TransferTransaction;

/**
 * author Facjas
 * date 2018/3/17.
 */
public class AmountValidator implements NulsDataValidator<AbstractCoinTransaction> {

    private static final AmountValidator INSTANCE = new AmountValidator();

    public static AmountValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(AbstractCoinTransaction tx) {
        UtxoData data = (UtxoData) tx.getCoinData();
        if (tx.getType() == TransactionConstant.TX_TYPE_TRANSFER) {
            long inTotal = 0;
            for (int i = 0; i < data.getInputs().size(); i++) {
                inTotal += data.getInputs().get(i).getFrom().getValue();
            }

            long outTotal = 0;
            for (int i = 0; i < data.getOutputs().size(); i++) {
                {
                    long amount = data.getOutputs().get(i).getValue();
                    //todo:  validate overflow attack
                    if (amount > Na.MAX_NA_VALUE) {
                        return ValidateResult.getFailedResult(ErrorCode.INVALID_AMOUNT);
                    }
                    outTotal += amount;
                }
                //todo:  fee must be calculated every yeah
            }
            long fee = 1000000;
            if (outTotal + fee > inTotal) {
                return ValidateResult.getFailedResult(ErrorCode.INVALID_AMOUNT);
            }
        } else {// coinbase
            //todo :validate the amount for every staker
            //todo :validata the coinbase is legal
        }
        return ValidateResult.getSuccessResult();
    }
}
