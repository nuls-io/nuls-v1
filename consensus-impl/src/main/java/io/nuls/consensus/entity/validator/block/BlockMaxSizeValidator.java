package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * Created by Niels on 2017/11/17.
 */
public class BlockMaxSizeValidator implements NulsDataValidator<Block> {
    private static final int MAX_SIZE = 2 << 21;//2M
    private static final String ERROR_MESSAGE = "The block is too big!";

    @Override
    public ValidateResult validate(Block data) {
        if (data == null) {
            return new ValidateResult(false, "Data is null!");
        }
        int length = data.size();
        if (length >= MAX_SIZE) {
            return ValidateResult.getFaildResult(ERROR_MESSAGE);
        }
        return ValidateResult.getSuccessResult();
    }
}
