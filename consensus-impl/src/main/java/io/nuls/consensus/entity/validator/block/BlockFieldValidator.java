package io.nuls.consensus.entity.validator.block;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/11/17
 */
public class BlockFieldValidator implements NulsDataValidator<Block> {
    private static final String ERROR_MESSAGE = "block field check failed";
    public static final BlockFieldValidator INSTANCE = new BlockFieldValidator();

    private BlockFieldValidator() {
    }

    public static BlockFieldValidator getInstance() {
        return INSTANCE;
    }

    @Override
    public ValidateResult validate(Block data) {
        ValidateResult result = ValidateResult.getSuccessResult();
        boolean failed = false;
        do {
            if (data == null) {
                failed = true;
                break;
            }
            if (data.getHeader() == null) {
                failed = true;
                break;
            }
            if (data.getTxs() == null || data.getTxs().isEmpty()) {
                failed = true;
                break;
            }

            if(data.getHeader().getTxCount()==0||data.getTxs().size()!=data.getHeader().getTxCount()){
                failed = true;
                break;
            }

        } while (false);
        if (failed) {
            result = ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return result;
    }
}
