package io.nuls.core.validate;

import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/16
 */
public class ValidateResult extends Result {

    private SeverityLevelEnum level;

    public static ValidateResult getFailedResult(String msg) {
        return getFailedResult(SeverityLevelEnum.NORMAL, msg);
    }

    public static ValidateResult getFailedResult(SeverityLevelEnum level, String msg) {
        ValidateResult result = new ValidateResult();
        result.setSuccess(false);
        result.setErrorCode(ErrorCode.VERIFICATION_FAILD);
        result.setMessage(msg);
        result.setLevel(level);
        return result;
    }

    public static ValidateResult getSuccessResult() {
        ValidateResult result = new ValidateResult();
        result.setSuccess(true);
        result.setMessage("");
        return result;
    }

    public static ValidateResult getFailedResult(ErrorCode msg) {
        return getFailedResult(SeverityLevelEnum.NORMAL, msg);
    }

    public static ValidateResult getFailedResult(SeverityLevelEnum level, ErrorCode errorCode) {
        ValidateResult result = new ValidateResult();
        result.setSuccess(false);
        result.setLevel(level);
        result.setErrorCode(errorCode);
        return result;
    }

    public SeverityLevelEnum getLevel() {
        return level;
    }

    public void setLevel(SeverityLevelEnum level) {
        this.level = level;
    }
}
