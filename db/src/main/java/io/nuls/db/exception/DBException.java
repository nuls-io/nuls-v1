package io.nuls.db.exception;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsRuntimeException;

/**
 * Created by zhouwei on 2017/9/30.
 */

public class DBException extends NulsRuntimeException {

    public DBException(ErrorCode message) {
        super(message);
    }
    public DBException(ErrorCode message,String msg) {
        super(message,msg);
    }
    public DBException(Throwable e) {
        super(e);
    }
}