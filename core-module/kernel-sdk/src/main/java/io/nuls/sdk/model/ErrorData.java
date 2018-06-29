package io.nuls.sdk.model;

import io.nuls.sdk.constant.ErrorCode;

/**
 * @author: Charlie
 */
public class ErrorData {

    private String code;

    private String msg;

    public ErrorData() {
    }

    public ErrorData(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ErrorData(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
    }

    public static ErrorData getErrorData(ErrorCode errorCode) {
        return new ErrorData(errorCode);
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
