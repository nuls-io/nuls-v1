package io.nuls.core.exception;

import io.nuls.core.constant.ErrorCode;

/**
 * Created by facjas on 2017/10/31.
 */
public class NulsVerificationException extends NulsRuntimeException {

    public NulsVerificationException(String msg) {
        super(ErrorCode.VERIFICATION_FAILD,msg);
    }
    public NulsVerificationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NulsVerificationException(ErrorCode errorCode, Throwable e) {
        super(errorCode, e);
    }

    public NulsVerificationException(ErrorCode errorCode, String msg) {
        super(errorCode, msg);
    }
}
