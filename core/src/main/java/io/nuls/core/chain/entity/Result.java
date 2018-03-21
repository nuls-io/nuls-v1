/**
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
 */
package io.nuls.core.chain.entity;

import io.nuls.core.constant.ErrorCode;

/**
 * @author vivi
 * @date 2017/12/12.
 */
public class Result<T> {

    private boolean success;

    private String message;

    private ErrorCode errorCode;

    private T object;

    public Result(boolean success, String message, ErrorCode errorCode, T object) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.object = object;
    }

    public Result() {
        this(false, "", ErrorCode.SUCCESS, null);
    }

    public Result(boolean success, String message) {
        this(success, message, ErrorCode.SUCCESS, null);
    }

    public Result(boolean success, ErrorCode errorCode, T object) {
        this.success = success;
        this.errorCode = errorCode;
        this.object = object;
    }

    public Result(boolean success, String message, T t) {
        this(success, message, ErrorCode.SUCCESS, t);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailed() {
        return !success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("result:{");
        buffer.append("success: " + success + ",");
        buffer.append("message: " + message + ",");
        if (errorCode == null) {
            buffer.append("errorCode: ");
        } else {
            buffer.append("errorCode: " + errorCode.getCode());
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static Result getFailed(String msg) {
        return new Result(false, msg);
    }

    public static Result getSuccess() {
        return new Result(true, "");
    }

    public static Result getFailed(ErrorCode errorCode) {
        return new Result(false, errorCode.getMsg());
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
