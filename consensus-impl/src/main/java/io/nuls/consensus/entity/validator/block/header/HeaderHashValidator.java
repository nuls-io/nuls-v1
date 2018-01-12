package io.nuls.consensus.entity.validator.block.header;

import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

import java.io.IOException;

/**
 * @author Niels
 * @date 2018/1/11
 */
public class HeaderHashValidator implements NulsDataValidator<BlockHeader> {

    private static final HeaderHashValidator INSTANCE = new HeaderHashValidator();
    private static final String ERROR_MESSAGE = "block header hash check failed";

    private HeaderHashValidator() {
    }

    @Override
    public ValidateResult validate(BlockHeader data) {
        ValidateResult result = ValidateResult.getSuccessResult();
        NulsDigestData hash = data.getHash();
        data.setSign(null);
        NulsDigestData cfmHash = null;
        try {
            cfmHash = NulsDigestData.calcDigestData(data.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        if (!cfmHash.getDigestHex().equals(hash.getDigestHex())) {
            result = ValidateResult.getFailedResult(ERROR_MESSAGE);
        }
        return result;
    }

    public static HeaderHashValidator getInstance() {
        return INSTANCE;
    }
}
