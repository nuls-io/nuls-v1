/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.contract.rpc.cmd;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Delete contract
 * Created by wangkun23 on 2018/9/21.
 */
public class DeleteContractProcessor implements CommandProcessor {

    /**
     * rest client utils
     */
    private RestFulUtils restFulUtils = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "deletecontract";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> contract address -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "deletecontract <sender> <address> --delete contract";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 3) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String sender = args[1];
        if (StringUtils.isBlank(sender)) {
            return CommandResult.getFailed(KernelErrorCode.PARAMETER_ERROR.getMsg());
        }
        String password = CommandHelper.getPwd();
        /**
         * assemble request body JSON
         */
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("sender", args[1]);
        parameters.put("contractAddress", args[2]);
        parameters.put("password", password);
        String url = "/contract/delete";
        RpcClientResult result = restFulUtils.post(url, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }
}
