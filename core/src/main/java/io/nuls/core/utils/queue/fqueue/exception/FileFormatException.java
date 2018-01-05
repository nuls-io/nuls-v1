package io.nuls.core.utils.queue.fqueue.exception;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;

/**
 * @author
 */
public class FileFormatException extends NulsException {

    /**
     *
     */
    private static final long serialVersionUID = -1L;

    public FileFormatException(String message) {
        super(ErrorCode.FAILED, message);
    }

    public FileFormatException(Throwable cause) {
        super(cause);
    }
}