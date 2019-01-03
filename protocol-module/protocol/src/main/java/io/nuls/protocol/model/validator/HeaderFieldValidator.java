/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.BlockHeader;
import io.nuls.kernel.validate.NulsDataValidator;
import io.nuls.kernel.validate.ValidateResult;
import io.nuls.protocol.constant.ProtocolErroeCode;

/**
 * @author Niels
 */
@Component
public class HeaderFieldValidator implements NulsDataValidator<BlockHeader> {

    private static final int OLD_HEADER_EXTENDS_MAS_SIZE = 64;
    private static final int HEADER_EXTENDS_MAS_SIZE = 1024;

    @Override
    public ValidateResult validate(BlockHeader data) {
        ValidateResult result = ValidateResult.getSuccessResult();
        boolean failed = false;
        do {
            if (data.getHash() == null) {
                failed = true;
                break;
            }
            if (data.getHeight() < 0) {
                failed = true;
                break;
            }
            if (data.getMerkleHash() == null) {
                failed = true;
                break;
            }
            if (null == data.getPackingAddress()) {
                failed = true;
                break;
            }
            if (null != data.getExtend()) {
                if (NulsContext.MAIN_NET_VERSION <= 1 && data.getExtend().length > OLD_HEADER_EXTENDS_MAS_SIZE) {
                    failed = true;
                    break;
                }
                if (data.getExtend().length > HEADER_EXTENDS_MAS_SIZE) {
                    failed = true;
                    break;
                }
            }
        } while (false);
        if (failed) {
            result = ValidateResult.getFailedResult(this.getClass().getName(), ProtocolErroeCode.BLOCK_HEADER_FIELD_CHECK_FAILED);
        }
        return result;
    }

}
