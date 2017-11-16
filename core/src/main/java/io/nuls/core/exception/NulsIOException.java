package io.nuls.core.exception;

import io.nuls.core.constant.ErrorCode;

/**
 * Created by facjas on 2017/11/16.
 */
public class NulsIOException extends NulsException {
    public NulsIOException(ErrorCode message) {
        super(message);
    }
}
