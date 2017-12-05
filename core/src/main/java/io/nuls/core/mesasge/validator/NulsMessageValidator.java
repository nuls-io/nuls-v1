package io.nuls.core.mesasge.validator;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.validate.NulsDataValidator;
import io.nuls.core.validate.ValidateResult;

/**
 * @author Niels
 * @date 2017/12/4
 */
public class NulsMessageValidator {
    public ValidateResult validate(NulsMessage data) {
        if (data.getHeader() == null || data.getData() == null) {
            return ValidateResult.getFaildResult(ErrorCode.NET_MESSAGE_ERROR);
        }

        if (data.getHeader().getLength() != data.getData().length) {
            return ValidateResult.getFaildResult(ErrorCode.NET_MESSAGE_LENGTH_ERROR);
        }

        if (data.getHeader().getXor() != data.caculateXor()) {
            return ValidateResult.getFaildResult(ErrorCode.NET_MESSAGE_XOR_ERROR);
        }
        return ValidateResult.getSuccessResult();
    }
}
