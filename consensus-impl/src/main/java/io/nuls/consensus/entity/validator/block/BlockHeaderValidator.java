package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2018/1/11
 */
public class BlockHeaderValidator implements NulsDataValidator<Block> {

    private static final BlockHeaderValidator INSTANCE = new BlockHeaderValidator();

    private BlockHeaderValidator() {
    }

    public static BlockHeaderValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Block data) {
        if(null==data||data.getHeader()==null){
            return ValidateResult.getFailedResult(ErrorCode.NULL_PARAMETER);
        }
        return data.getHeader().verify();
    }
}
