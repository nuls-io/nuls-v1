/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.kernel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;

import java.util.Map;

/**
 * @author Niels
 */
public class RpcClientResult {

    private boolean success;

    private Object data;

    public RpcClientResult() {

    }

    public RpcClientResult(boolean success, ErrorData errorData) {
        this.success = success;
        this.data = errorData;
    }

    public RpcClientResult(boolean success, ErrorCode errorCode) {
        this.success = success;
        this.data = ErrorData.getErrorData(errorCode);
    }

    public RpcClientResult(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    public static RpcClientResult getFailed(ErrorData errorData) {
        return new RpcClientResult(false, errorData);
    }

    public static RpcClientResult getFailed(ErrorCode errorCode) {
        return new RpcClientResult(false, errorCode);
    }

    public static RpcClientResult getFailed(String msg) {
        return getFailed(new ErrorData(KernelErrorCode.FAILED.getCode(), msg));
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return null;
        }

    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
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

    public boolean dataToBooleanValue() {
        return (boolean) ((Map) data).get("value");
    }

    public String dataToStringValue() {
        Object object = ((Map) data).get("value");
        if (null != object) {
            return (String) object;
        }
        return null;
    }
}
