package io.nuls.core.validate;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/11/16
 */
public class ValidateResult {
    private boolean seccess;
    private String message;

    public ValidateResult(boolean seccess, String message) {
        this.seccess = seccess;
        this.message = message;
    }

    public boolean isSeccess() {
        return seccess;
    }

    public void setSeccess(boolean seccess) {
        this.seccess = seccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static ValidateResult getFaildResult(String msg) {
        return new ValidateResult(false, msg);
    }

    public static ValidateResult getSuccessResult() {
        return new ValidateResult(true, "");
    }

    public static ValidateResult getFaildResult(ErrorCode errorCode) {
        return new ValidateResult(false,errorCode.getMsg());
    }
}
