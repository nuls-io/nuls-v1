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
 * Transfer to contract address
 * Created by wangkun23 on 2018/9/25.
 */
public class TransferToContractProcessor implements CommandProcessor {
    /**
     * rest utils
     */
    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "transfertocontract";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> address -required")
                .newLine("\t<toAddress> toAddress -required")
                .newLine("\t<gasLimit> gasLimit -required")
                .newLine("\t<price> contract price -required")
                .newLine("\t<amount> transfer amount -required")
                .newLine("\t[remark] remark not -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "transfertocontract <address> <toAddress> <gasLimit> <price> <amount> [remark] --create transfer to contract address";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length <6) {
            return false;
        }
        if (length >7) {
            return false;
        }
        return true;
    }

    /**
     * {
     * "address": "Nsdv1Hbu4TokdgbXreypXmVttYKdPT1g",
     * "toAddress": "NseDqffhWEB52a9cWfiyEhiP3wPGcjcJ",
     * "gasLimit": 800000,
     * "price": 27,
     * "password": "nuls123456",
     * "amount": 10000000,
     * "remark": ""
     * }
     *
     * @param args
     * @return
     */
    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        if (StringUtils.isBlank(address)) {
            return CommandResult.getFailed(KernelErrorCode.PARAMETER_ERROR.getMsg());
        }
        RpcClientResult res = CommandHelper.getPassword(address, restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String) res.getData();
        /**
         * assemble request body JSON
         */
        Map<String, Object> parameters = new HashMap<>(7);
        parameters.put("address", address);
        parameters.put("toAddress", args[2]);
        parameters.put("gasLimit", args[3]);
        parameters.put("price", args[4]);
        parameters.put("amount", args[5]);
        if (args.length==7){
            parameters.put("remark",args[6]);
        }
        //password
        parameters.put("password", password);

        String url = "/contract/transfer";
        RpcClientResult result = restFul.post(url, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }
}
