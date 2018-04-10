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
package io.nuls.consensus.entity.validator;

import io.nuls.consensus.entity.validator.block.*;
import io.nuls.consensus.entity.validator.block.header.HeaderFieldValidator;
import io.nuls.consensus.entity.validator.block.header.HeaderHashValidator;
import io.nuls.consensus.entity.validator.block.header.HeaderPackerValidator;
import io.nuls.consensus.entity.validator.block.header.HeaderSignValidator;
import io.nuls.core.chain.manager.BlockHeaderValidatorManager;
import io.nuls.core.chain.manager.BlockValidatorManager;

/**
 * @author Niels
 * @date 2017/12/7
 */
public class PocBlockValidatorManager {

    public static void initHeaderValidators(){
        BlockHeaderValidatorManager.addBlockDefValitor(HeaderFieldValidator.getInstance());
        BlockHeaderValidatorManager.addBlockDefValitor(HeaderHashValidator.getInstance());
        BlockHeaderValidatorManager.addBlockDefValitor(HeaderContinuityValidator.getInstance());
        BlockHeaderValidatorManager.addBlockDefValitor(HeaderSignValidator.getInstance());
        BlockHeaderValidatorManager.addBlockDefValitor(HeaderPackerValidator.getInstance());
    }

    public static void initBlockValidators() {

        BlockValidatorManager.addBlockDefValitor(BlockMaxSizeValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockHeaderValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockFieldValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockTxValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockMerkleValidator.getInstance());

        BlockValidatorManager.addBlockDefValitor(BlockConsensusValidator.getInstance());

    }
}
