package io.nuls.consensus.entity.validator.block.header;

import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2018/1/11
 */
public class HeaderFieldValidator implements NulsDataValidator<BlockHeader> {

    private static final HeaderFieldValidator INSTANCE = new HeaderFieldValidator();
    private static final String ERROR_MESSAGE = "block header field check failed";

    private HeaderFieldValidator() {
    }

    @Override
    public ValidateResult validate(BlockHeader data) {
        ValidateResult result = ValidateResult.getSuccessResult();
        boolean failed = false;
        do {
            if (data.getSign() == null) {
                failed = true;
                break;
            }
            if (data.getHash() == null) {
                failed = true;
                break;
            }
            if (data.getHeight() < 0) {
                failed = true;
                break;
            }
            if (data.getMerkleHash() == null) {
                failed = true;
                break;
            }
            if (data.getPackingAddress() == null) {
                failed = true;
                break;
            }
        } while (false);
        if (failed) {
            result = ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return result;
    }

    public static HeaderFieldValidator getInstance() {
        return INSTANCE;
    }
}
