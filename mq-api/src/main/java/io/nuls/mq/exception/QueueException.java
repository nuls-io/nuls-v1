package io.nuls.mq.exception;

import io.nuls.exception.InchainException;

/**
 * Created by Niels on 2017/9/20.
 */
public class QueueException extends InchainException {

    public QueueException(String message) {
        super(message);
    }
    public QueueException(Throwable e) {
        super(e);
    }
}
