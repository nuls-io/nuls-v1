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
package io.nuls.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuls.sdk.constant.ErrorCode;
import io.nuls.sdk.constant.KernelErrorCode;

/**
 * Mapping RpcClientResult
 * @author Charlie
 * @date 2018/6/8
 */
public class Result {

    private boolean success;

    private String code;

    private String msg;

    private Object data;

    public Result() {

    }
    public Result(boolean success, ErrorCode errorCode) {
        this.success = success;
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
    }

    public Result(boolean success, String code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return null;
        }

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }

    @JsonIgnore
    public boolean isFailed() {
        return !success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


    public static Result getFailed() {
        return getFailed(KernelErrorCode.FAILED);
    }

    public static Result getFailed(String msg) {
        return new Result(false, KernelErrorCode.FAILED.getCode(), msg);
    }

    public static Result getSuccess() {
        return new Result(true, KernelErrorCode.SUCCESS);
    }

    public static Result getFailed(ErrorCode errorCode) {
        return getFailed(errorCode, errorCode.getMsg());
    }
    public static Result getFailed(ErrorCode errorCode, String msg) {
        Result result = new Result(false, errorCode);
        result.setCode(errorCode.getCode());
        result.setMsg(errorCode.getMsg());
        return result;
    }
}
