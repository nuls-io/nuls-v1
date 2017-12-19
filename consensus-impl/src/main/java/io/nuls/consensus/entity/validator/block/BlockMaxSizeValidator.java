package io.nuls.consensus.entity.validator.block;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class BlockMaxSizeValidator implements NulsDataValidator<Block> {
    public static final BlockMaxSizeValidator INSTANCE = new BlockMaxSizeValidator();

    private static final String ERROR_MESSAGE = "The block is too big!";

    private BlockMaxSizeValidator(){}
    public static BlockMaxSizeValidator getInstance(){
        return INSTANCE;
    }
    @Override
    public ValidateResult validate(Block data) {
        if (data == null) {
            return ValidateResult.getFailedResult("Data is null!");
        }
        int length = data.size();
        if (length >= PocConsensusConstant.MAX_BLOCK_SIZE) {
            return ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
