package io.nuls.consensus.entity.validator.block.header;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class HeaderSignValidator implements NulsDataValidator<BlockHeader> {
    private static final String ERROR_MESSAGE = "block header sign check failed";
    public static final HeaderSignValidator INSTANCE = new HeaderSignValidator();
    private HeaderSignValidator(){}
    public static HeaderSignValidator getInstance(){
        return INSTANCE;
    }
    @Override
    public ValidateResult validate(BlockHeader data) {
         //todo 
        return ValidateResult.getSuccessResult();
    }
}
