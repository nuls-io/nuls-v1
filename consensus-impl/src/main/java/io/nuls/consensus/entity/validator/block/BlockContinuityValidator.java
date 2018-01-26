/**
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
package io.nuls.consensus.entity.validator.block;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class BlockContinuityValidator implements NulsDataValidator<Block> {
    private static final String ERROR_MESSAGE = "block continuity check failed";
    public static final BlockContinuityValidator INSTANCE = new BlockContinuityValidator();

    private BlockContinuityValidator() {
    }

    public static BlockContinuityValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Block block) {
        ValidateResult result = ValidateResult.getSuccessResult();
        boolean failed;
        do {
            if (block.getHeader().getHeight() == 0) {
                failed = !block.getHeader().getPreHash().equals(NulsDigestData.EMPTY_HASH);
                break;
            }
            Block preBlock = NulsContext.getServiceBean(BlockService.class).getBlock(block.getHeader().getHeight()-1);
            failed = !preBlock.getHeader().getHash().equals(block.getHeader().getPreHash());
            if(failed){
                break;
            }
            failed = preBlock.getHeader().getTime()>(block.getHeader().getTime()- PocConsensusConstant.BLOCK_TIME_INTERVAL*1000);
        } while (false);

        if (failed) {
            result = ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return result;
    }
}
