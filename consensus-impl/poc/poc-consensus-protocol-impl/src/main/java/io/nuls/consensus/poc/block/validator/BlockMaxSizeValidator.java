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

import io.nuls.consensus.poc.protocol.constant.PocConsensusConstant;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Transaction;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class BlockMaxSizeValidator implements NulsDataValidator<Block> {
    public static final BlockMaxSizeValidator INSTANCE = new BlockMaxSizeValidator();

    private static final String ERROR_MESSAGE = "The block is too big!";

    private BlockMaxSizeValidator() {
    }

    public static BlockMaxSizeValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Block data) {
        if (data == null) {
            return ValidateResult.getFailedResult("Data is null!");
        }
        long length = 0L;
        for (Transaction tx : data.getTxs()) {
            length += tx.size();
        }
        if (length >= PocConsensusConstant.MAX_BLOCK_SIZE) {
            return ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
