package io.nuls.mq.exception;

import io.nuls.exception.NulsException;

/**
 * Created by Niels on 2017/9/20.
 */
public class QueueException extends NulsException {

    public QueueException(String message) {
        super(message);
    }
    public QueueException(Throwable e) {
        super(e);
    }
}
