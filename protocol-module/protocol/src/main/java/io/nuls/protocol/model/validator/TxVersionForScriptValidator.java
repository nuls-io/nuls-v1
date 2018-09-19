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
package io.nuls.protocol.model.validator;

import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Coin;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.util.List;

/**
 * @author Niels
 */
@Component
public class TxVersionForScriptValidator implements NulsDataValidator<Transaction> {
    public final static int MAX_REMARK_LEN = 100;


    @Override
    public ValidateResult validate(Transaction tx) {
        if (NulsContext.MAIN_NET_VERSION > 1) {
            return ValidateResult.getSuccessResult();
        }
        if (null == tx.getCoinData() || tx.getCoinData().getTo() == null || tx.getCoinData().getTo().isEmpty()) {
            return ValidateResult.getSuccessResult();
        }
        List<Coin> toList = tx.getCoinData().getTo();
        ValidateResult failed = ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.VERSION_NOT_NEWEST);
        for (Coin coin : toList) {
            if (coin.getOwner().length != Address.ADDRESS_LENGTH) {
                return failed;
            }
            if (coin.getOwner()[2] != NulsContext.DEFAULT_ADDRESS_TYPE) {
                return failed;
            }
            byte[] chainId = SerializeUtils.shortToBytes(NulsContext.DEFAULT_CHAIN_ID);
            if (chainId[0] != coin.getOwner()[0] || chainId[1] != coin.getOwner()[1]) {
                return failed;
            }
        }
        return ValidateResult.getSuccessResult();
    }
}
