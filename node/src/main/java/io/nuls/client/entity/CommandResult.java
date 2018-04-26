/*
 *
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

package io.nuls.client.entity;

import io.nuls.core.utils.json.JSONUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.sdk.entity.RpcClientResult;

/**
 * @author Niels
 * @date 2018/3/7
 */
public class CommandResult {

    public boolean success;

    private String message;

    public boolean isSuccess() {
        return success;
    }

    public CommandResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CommandResult setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        if (StringUtils.isBlank(message)) {
            return "result:" + success;
        } else {
            return message;
        }
    }

    public static CommandResult getFailed(String message) {
        CommandResult result = new CommandResult();
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    public static CommandResult getResult(RpcClientResult rpcResult) {
        if (null == rpcResult) {
            return CommandResult.getFailed("Result is null!");
        }
        CommandResult result = new CommandResult();
        result.setSuccess(rpcResult.isSuccess());
        String message = rpcResult.getMsg();
        if (StringUtils.isBlank(message)) {
            message = "";
        } else {
            message += ";\n";
        }
        if (rpcResult.getData() != null) {
            try {
                message += JSONUtils.obj2PrettyJson(rpcResult.getData());
            } catch (Exception e) {
                Log.error(e);
            }
        }
        result.setMessage(message);
        return result;
    }

    public static CommandResult getSuccess(String message) {
        return new CommandResult().setSuccess(true).setMessage(message);
    }
}
