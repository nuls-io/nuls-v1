/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.rpc.entity;


import io.nuls.core.chain.entity.Result;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/9/27
 */
public class RpcResult<T> {

    private boolean success;
    private String code;
    private String msg;
    private T data;

    public RpcResult() {
    }

    public RpcResult(boolean success, String code, String msg) {
        this.success = success;
        this.code = code;
        this.msg = msg;
    }

    public RpcResult(boolean success, String code, String msg, T data) {
        this.success = success;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public RpcResult(boolean success, ErrorCode ec) {
        this.success = success;
        this.code = ec.getCode() + "";
        this.msg = ec.getMsg();
    }

    public RpcResult(Result result) {
        this.success = result.isSuccess();
        if (result.isSuccess()) {
            this.code = ErrorCode.SUCCESS.getCode();
        } else {
            this.code = ErrorCode.FAILED.getCode();
        }
        this.msg = result.getMessage();
        this.data = (T) result.getObject();
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public RpcResult setCode(String code) {
        this.code = code;
        return this;
    }

    public RpcResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public RpcResult setData(T data) {
        this.data = data;
        return this;
    }

    public static RpcResult getSuccess() {
        return new RpcResult(true, ErrorCode.SUCCESS);
    }

    public static RpcResult getFailed() {
        return new RpcResult(false, ErrorCode.FAILED);
    }

    public static RpcResult getFailed(String msg) {
        return new RpcResult(false, ErrorCode.FAILED.getCode(), msg);
    }

    public static RpcResult getFailed(ErrorCode errorCode) {
        return new RpcResult(false, errorCode);
    }

    @Override
    public String toString() {
        try {
            return JSONUtils.obj2json(this);
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
