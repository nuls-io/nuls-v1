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
package io.nuls.protocol.model.validator;

import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.constant.ProtocolErroeCode;

/**
 * @author Niels
 */
@Component
public class BlockFieldValidator implements NulsDataValidator<Block> {

    @Override
    public ValidateResult validate(Block data) {
        ValidateResult result = ValidateResult.getSuccessResult();
        boolean failed = false;
        do {
            if (data == null) {
                failed = true;
                break;
            }
            if (data.getHeader() == null) {
                failed = true;
                break;
            }
            if (data.getTxs() == null || data.getTxs().isEmpty()) {
                failed = true;
                break;
            }

            if(data.getHeader().getTxCount() == 0 || data.getTxs().size() != data.getHeader().getTxCount()){
                failed = true;
                break;
            }

        } while (false);
        if (failed) {
            result = ValidateResult.getFailedResult(this.getClass().getName(), ProtocolErroeCode.BLOCK_FIELD_CHECK_FAILED);
        }
        return result;
    }
}
