package io.nuls.mq.exception;

import io.nuls.exception.NulsRuntimeException;
import io.nuls.util.constant.ErrorCode;

/**
 * Created by Niels on 2017/9/20.
 */
public class QueueException extends NulsRuntimeException {

    public QueueException(ErrorCode message) {
        super(message);
    }public QueueException(ErrorCode message,String msg) {
        super(message,msg);
    }
    public QueueException(Throwable e) {
        super(e);
    }
}
