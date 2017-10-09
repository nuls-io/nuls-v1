package io.nuls.mq.exception;

import io.nuls.exception.NulsRuntimeException;

/**
 * Created by Niels on 2017/9/20.
 */
public class QueueException extends NulsRuntimeException {

    public QueueException(String message) {
        super(message);
    }
    public QueueException(Throwable e) {
        super(e);
    }
}
