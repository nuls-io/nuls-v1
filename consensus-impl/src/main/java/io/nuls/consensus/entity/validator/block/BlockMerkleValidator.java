package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * Created by Niels on 2017/11/17.
 */
public class BlockMerkleValidator implements NulsDataValidator<Block> {
    private static final String ERROR_MESSAGE = "";

    @Override
    public ValidateResult validate(Block data) {
         //todo 
        return ValidateResult.getSuccessResult();
    }
}
