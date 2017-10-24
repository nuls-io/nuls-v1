package io.nuls.mq.fqueue.exception;

/**
 * @author opensource
 */
public class FileEOFException extends Exception {

    private static final long serialVersionUID = -1L;

    public FileEOFException() {
        super();
    }

    public FileEOFException(String message) {
        super(message);
    }

    public FileEOFException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileEOFException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}