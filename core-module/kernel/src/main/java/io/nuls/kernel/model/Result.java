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
package io.nuls.kernel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;

import java.io.Serializable;

/**
 * @author vivi
 * @date 2017/12/12.
 */
public class Result<T> implements Serializable {

    private boolean success;

    private String msg;

    private ErrorCode errorCode;

    private T data;

    public Result(boolean success, String message, ErrorCode errorCode, T data) {
        this.success = success;
        this.msg = message;
        this.errorCode = errorCode;
        this.data = data;
    }

    public Result() {
        this(false, "", KernelErrorCode.SUCCESS, null);
    }

    public Result(boolean success, ErrorCode code, String message) {
        this(success, message, code, null);
    }

    public Result(boolean success, ErrorCode errorCode, T data) {
        this.success = success;
        this.errorCode = errorCode;
        this.data = data;
    }

    public Result(boolean success, String message, T t) {
        this(success, message, KernelErrorCode.SUCCESS, t);
    }

    public boolean isSuccess() {
        return success;
    }

    @JsonIgnore
    public boolean isFailed() {
        return !success;
    }

    public Result<T> setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getMsg() {
        if (StringUtils.isBlank(msg)) {
            return errorCode.getMsg();
        }
        return msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
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
        buffer.append("\"success\": " + success + ",");
        buffer.append("\"validator\": \"" + msg + "\",");
        if (errorCode == null) {
            buffer.append("\"errorCode\": \"\",");
        } else {
            buffer.append("\"errorCode\": \"" + errorCode.getCode() + "\",");
        }
        if (data != null) {
            try {
                buffer.append("\"data\":" + JSONUtils.obj2json(data));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static Result getFailed() {
        return getFailed(KernelErrorCode.FAILED);
    }

    public static Result getFailed(String msg) {
        return new Result(false, KernelErrorCode.FAILED, msg);
    }

    public static Result getSuccess() {
        return new Result(true, KernelErrorCode.SUCCESS, "");
    }

    public static Result getFailed(ErrorCode errorCode) {
        return getFailed(errorCode, errorCode.getMsg());
    }

    public static Result getFailed(ErrorCode errorCode, String msg) {
        Result result = new Result(false, KernelErrorCode.FAILED, msg);
        result.setErrorCode(errorCode);
        return result;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    public RpcClientResult toRpcClientResult() {
        RpcClientResult rpcClientResult = new RpcClientResult();
        rpcClientResult.setCode(this.errorCode.getCode());
        if (errorCode == null) {
            rpcClientResult.setMsg(msg);
        } else {
            rpcClientResult.setMsg(this.errorCode.getMsg());
        }
        rpcClientResult.setSuccess(success);
        rpcClientResult.setData(data);
        return rpcClientResult;
    }
}
