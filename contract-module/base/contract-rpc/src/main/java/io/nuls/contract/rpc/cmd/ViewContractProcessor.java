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

package io.nuls.contract.rpc.cmd;

import io.nuls.contract.rpc.form.ContractViewCall;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.kernel.utils.CommandHelper.getContractCallArgsJson;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/9/19
 */
public class ViewContractProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    private ThreadLocal<ContractViewCall> paramsData = new ThreadLocal<>();

    @Override
    public String getCommand() {
        return "viewcontract";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<contractAddress>   contract address    -required")
                .newLine("\t<methodName>        the method to call    -required")
                .newLine("\t[-d methodDesc]        the method description    -not required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "viewcontract <contractAddress> <methodName> [-d methodDesc] --view contract";
    }

    @Override
    public boolean argsValidate(String[] args) {
        boolean result;
        do {
            int length = args.length;
            if (length != 3 && length != 5) {
                result = false;
                break;
            }
            if (!CommandHelper.checkArgsIsNull(args)) {
                result = false;
            break;
            }

            ContractViewCall form = getContractViewCall(args);
            if(null == form){
                result = false;
                break;
            }
            paramsData.set(form);

            result = true;
        } while (false);
        return result;
    }

    private ContractViewCall getContractViewCall(String[] args) {
        ContractViewCall call;
        try {
            call = new ContractViewCall();
            call.setContractAddress(args[1].trim());
            call.setMethodName(args[2].trim());

            if(args.length == 5) {
                String argType = args[3].trim();
                if(argType.equals("-d")) {
                    call.setMethodDesc(args[4].trim());
                } else {
                    return null;
                }
            }
            return call;
        } catch (Exception e) {
            e.fillInStackTrace();
            return null;
        }
    }


    @Override
    public CommandResult execute(String[] args) {
        ContractViewCall form = paramsData.get();
        if (null == form) {
            form = getContractViewCall(args);
        }
        if (null == form) {
            return CommandResult.getFailed("parameter error.");
        }
        RpcClientResult res = getContractCallArgsJson();
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        Object[] contractArgs = (Object[]) res.getData();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("contractAddress", form.getContractAddress());
        parameters.put("methodName", form.getMethodName());
        parameters.put("methodDesc", form.getMethodDesc());
        parameters.put("args", contractArgs);
        RpcClientResult result = restFul.post("/contract/view", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }


}
