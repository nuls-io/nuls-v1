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

import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.protocol.model.Block;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class BlockFieldValidator implements NulsDataValidator<Block> {
    private static final String ERROR_MESSAGE = "block field check failed";
    public static final BlockFieldValidator INSTANCE = new BlockFieldValidator();

    private BlockFieldValidator() {
    }

    public static BlockFieldValidator getInstance() {
        return INSTANCE;
    }

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

            if(data.getHeader().getTxCount()==0||data.getTxs().size()!=data.getHeader().getTxCount()){
                failed = true;
                break;
            }

        } while (false);
        if (failed) {
            result = ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return result;
    }
}
