package io.nuls.core.exception;


import io.nuls.core.constant.ErrorCode;

/**
 * @author Niels
 * @date 2017/9/26
 */
public class NulsRuntimeException extends RuntimeException {

    private String code;
    private String message;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public NulsRuntimeException(ErrorCode message) {
        super(message.getMsg());
        this.code = message.getCode();
        this.message = message.getMsg();
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     * @since 1.4
     */
    public NulsRuntimeException(ErrorCode message, Throwable cause) {
        super(message.getMsg(), cause);
        this.code = message.getCode();
        this.message = message.getMsg();
        ;
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail message of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public NulsRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message,
     * cause, suppression enabled or disabled, and writable stack
     * trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     * @since 1.7
     */
    protected NulsRuntimeException(ErrorCode message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message.getMsg(), cause, enableSuppression, writableStackTrace);
        this.code = message.getCode();
        this.message = message.getMsg();
    }

    public NulsRuntimeException(ErrorCode errorCode, String msg) {
        super(msg);
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg()+":"+msg;
    }
}
