package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class BlockSignValidator implements NulsDataValidator<Block> {
    private static final String ERROR_MESSAGE = "";
    public static final BlockSignValidator INSTANCE = new BlockSignValidator();
    private BlockSignValidator(){}
    public static BlockSignValidator getInstance(){
        return INSTANCE;
    }
    @Override
    public ValidateResult validate(Block data) {
         //todo 
        return ValidateResult.getSuccessResult();
    }
}
