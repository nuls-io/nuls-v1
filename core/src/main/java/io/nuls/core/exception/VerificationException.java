package io.nuls.core.exception;

import io.nuls.core.constant.ErrorCode;

/**
 * Created by win10 on 2017/10/31.
 */
public class VerificationException extends NulsRuntimeException {

    public VerificationException(String msg) {
        super(ErrorCode.VERIFICATION_FAILD,msg);
    }
    public VerificationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public VerificationException(ErrorCode errorCode, Throwable e) {
        super(errorCode, e);
    }

    public VerificationException(ErrorCode errorCode, String msg) {
        super(errorCode, msg);
    }
}
