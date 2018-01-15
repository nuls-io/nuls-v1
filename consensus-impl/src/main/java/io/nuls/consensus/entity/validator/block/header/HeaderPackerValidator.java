package io.nuls.consensus.entity.validator.block.header;

import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class HeaderPackerValidator implements NulsDataValidator<BlockHeader> {
    private static final String ERROR_MESSAGE = "block header packer check failed";
    public static final HeaderPackerValidator INSTANCE = new HeaderPackerValidator();
    private HeaderPackerValidator(){}
    public static HeaderPackerValidator getInstance(){
        return INSTANCE;
    }
    @Override
    public ValidateResult validate(BlockHeader data) {
         //todo 
        return ValidateResult.getSuccessResult();
    }
}
