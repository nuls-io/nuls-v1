package io.nuls.db;

import io.nuls.exception.NulsException;
import io.nuls.exception.NulsRuntimeException;
import io.nuls.util.constant.ErrorCode;

/**
 * Created by win10 on 2017/9/30.
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