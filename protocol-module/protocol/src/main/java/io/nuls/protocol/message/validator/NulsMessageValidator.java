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
package io.nuls.protocol.message.validator;

import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.message.base.BaseMessage;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class NulsMessageValidator {
    public ValidateResult validate(BaseMessage data) {
        if (data.getHeader() == null || data.getMsgBody() == null) {
            return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.NET_MESSAGE_ERROR);
        }

        if (data.getHeader().getLength() != data.getMsgBody().size()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.NET_MESSAGE_LENGTH_ERROR);
        }

        if (data.getHeader().getXor() != data.caculateXor()) {
            return ValidateResult.getFailedResult(this.getClass().getName(), KernelErrorCode.NET_MESSAGE_XOR_ERROR);
        }
        return ValidateResult.getSuccessResult();
    }
}
