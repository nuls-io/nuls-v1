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
package io.nuls.accout.ledger.rpc.cmd;

import io.nuls.accout.ledger.rpc.form.CreateP2shTransactionForm;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: tag
 */
public class CreateMultiTransferProcess implements CommandProcessor {
    private RestFulUtils restFul = RestFulUtils.getInstance();
    @Override
    public String getCommand() {
        return "createMultiTransfer";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> \t\tsource address - Required")
                .newLine("\t<signAddress> \tsign address address - Required")
                .newLine("\t<toAddress>,<toamount>;....;<toAddress><toamount> \tThe meaning of [toAddress],[toamount] is pay  toAddress toamount nuls," +
                        "Separate multiple [toAddress],[toamount],If there are multiple payee Separate multiple. - Required")
                .newLine("\t[remark] \t\tremark - Not Required");
        return builder.toString();

    }

    @Override
    public String getCommandDescription() {
        return "createMultiTransfer <address> <signAddress> <toAddress>,<toamount>;....;<toAddress><toamount> [remark] -createMultiTransfer-";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 4 && length != 5) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (StringUtils.isBlank(args[1]) || StringUtils.isBlank(args[2])) {
            return false;
        }
        if(!CreateP2shTransactionForm.validToData(args[3])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        RpcClientResult res = CommandHelper.getPassword(args[2], restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String)res.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address",args[1]);
        parameters.put("signAddress",args[2]);
        parameters.put("outputs",CreateP2shTransactionForm.getTodata(args[3]));
        if(args.length == 5){
            parameters.put("remark",args[4]);
        }
        parameters.put("password",password);
        RpcClientResult result = restFul.post("/accountledger/multiAccount/createMultiTransfer", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(CommandResult.dataMultiTransformValue(result));
    }
}
