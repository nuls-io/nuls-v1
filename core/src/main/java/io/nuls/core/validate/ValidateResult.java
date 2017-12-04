package io.nuls.core.validate;

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
}
