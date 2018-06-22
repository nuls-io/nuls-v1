/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.kernel.exception;


import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.ErrorCode;

/**
 * @author Niels
 * @date 2017/9/26
 */
public class NulsRuntimeException extends RuntimeException {

    private String code;
    private String message;
    private ErrorCode errorCode;

    /**
     * Constructs a new exception with the specified detail validator.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail validator. The detail validator is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public NulsRuntimeException(ErrorCode message) {
        super(message.getMsg());
        this.code = message.getCode();
        this.message = message.getMsg();
        this.errorCode = message;
    }

    /**
     * Constructs a new exception with the specified detail validator and
     * cause.  Note that the detail validator associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail validator.
     *
     * @param message the detail validator (which is saved for later retrieval
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
        this.errorCode = message;
        ;
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * validator of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail validator of <tt>cause</tt>).
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
     * Constructs a new exception with the specified detail validator,
     * cause, suppression enabled or disabled, and writable stack
     * trace enabled or disabled.
     *
     * @param message            the detail validator.
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
        this.errorCode = message;
    }

    public NulsRuntimeException(ErrorCode errorCode, String msg) {
        super(msg);
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg() + ":" + msg;
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        if (StringUtils.isBlank(message)) {
            return super.getMessage();
        }
        return message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
}

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
