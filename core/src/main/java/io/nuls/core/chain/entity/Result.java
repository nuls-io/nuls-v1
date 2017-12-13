package io.nuls.core.chain.entity;

import io.nuls.core.constant.ErrorCode;

/**
 * @author vivi
 * @date 2017/12/12.
 */
public class Result<T> {

    private boolean success;

    private String message;

    private ErrorCode errorCode;

    private T object;

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

    public Result(boolean success, String message, T t) {
        this.success = success;
        this.message = message;
        this.object = t;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFaild() {
        return !success;
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

    public static Result getFaildResult(String msg) {
        return new Result(false, msg);
    }

    public static Result getSuccessResult() {
        return new Result(true, "");
    }

    public static Result getFaildResult(ErrorCode errorCode) {
        return new Result(false,errorCode.getMsg());
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
