/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.protocol.model.validator;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Transaction;
import io.nuls.kernel.script.P2PKHScriptSig;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/20
 */
@Component
public class TxSignValidator implements NulsDataValidator<Transaction> {

    @Override
    public ValidateResult validate(Transaction tx) {
        if (!tx.needVerifySignature()) {
            return ValidateResult.getSuccessResult();
        }
        byte[] scriptSig = tx.getScriptSig();
        P2PKHScriptSig p2PKHScriptSig = null;
        try {
            p2PKHScriptSig = new NulsByteBuffer(scriptSig).readNulsData(new P2PKHScriptSig());
        } catch (Exception e) {
            return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.SIGNATURE_ERROR);
        }
        try {
            return p2PKHScriptSig.verifySign(tx.getHash().getDigestBytes());
        } catch (Exception e) {
            Log.error(e);
            return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.SIGNATURE_ERROR);
        }
    }
}
