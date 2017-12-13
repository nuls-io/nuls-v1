package io.nuls.core.validate;

import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/16
 */
public class ValidateResult extends Result{

    public static ValidateResult getFaildResult(String msg) {
        ValidateResult result= new ValidateResult();
        result.setSuccess(false);
        result.setMessage(msg);
        return result;
    }

    public static ValidateResult getSuccessResult() {
        ValidateResult result= new ValidateResult();
        result.setSuccess(true);
        result.setMessage("");
        return result;
    }

    public static ValidateResult getFaildResult(ErrorCode errorCode) {
        ValidateResult result= new ValidateResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        return result;
    }
}
