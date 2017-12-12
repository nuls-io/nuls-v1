package io.nuls.core.chain.entity;

import io.nuls.core.constant.ErrorCode;

/**
 * @author vivi
 * @date 2017/12/12.
 */
public class Result {

    private boolean success;

    private String message;

    private ErrorCode errorCode;

    public Result() {

    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Result(boolean success, String message, ErrorCode errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("result:{");
        buffer.append("success: " + success + ",");
        buffer.append("message: " + message + ",");
        if (errorCode == null) {
            buffer.append("errorCode: ");
        } else {
            buffer.append("errorCode: " + errorCode.getCode());
        }
        buffer.append("}");
        return buffer.toString();
    }
}
