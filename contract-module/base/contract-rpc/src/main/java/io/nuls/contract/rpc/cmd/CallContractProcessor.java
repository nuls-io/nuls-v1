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

import io.nuls.contract.rpc.form.ContractCall;
import io.nuls.contract.rpc.form.ContractCreate;
import io.nuls.core.tools.json.JSONUtils;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.kernel.utils.CommandHelper.getContractCallArgsJson;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/9/19
 */
public class CallContractProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    private ThreadLocal<ContractCall> paramsData = new ThreadLocal<>();

    @Override
    public String getCommand() {
        return "callcontract";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<sender>            source address    -required")
                .newLine("\t<gasLimit>          gas limit    -required")
                .newLine("\t<price>             price (Unit: Na/Gas)    -required")
                .newLine("\t<contractAddress>   contract address    -required")
                .newLine("\t<methodName>        the method to call    -required")
                .newLine("\t<value>             transfer nuls to the contract (Unit: Nuls)    -required")
                .newLine("\t[-d methodDesc]        the method description    -not required")
                .newLine("\t[-r remark]            remark    -not required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "callcontract <sender> <gasLimit> <price> <contractAddress> <methodName> <value> [-d methodDesc] [-r remark] --call contract";
    }

    @Override
    public boolean argsValidate(String[] args) {
        boolean result;
        do {
            int length = args.length;
            if (length != 7 && length != 9 && length != 11) {
                result = false;
                break;
            }
            if (!CommandHelper.checkArgsIsNull(args)) {
                result = false;
                break;
            }

            // gasLimit
            if (!StringUtils.isNumeric(args[2])) {
                result = false;
                break;
            }
            // price
            if (!StringUtils.isNumeric(args[3])) {
                result = false;
                break;
            }
            // value
            if (!StringUtils.isNumeric(args[6])) {
                result = false;
                break;
            }
            ContractCall form = getContractCall(args);
            if(null == form){
                result = false;
                break;
            }
            paramsData.set(form);

            result = form.getValue() >= 0;
        } while (false);
        return result;
    }

    private ContractCall getContractCall(String[] args) {
        ContractCall call = null;
        try {
            call = new ContractCall();
            call.setSender(args[1].trim());
            call.setGasLimit(Long.valueOf(args[2].trim()));
            call.setPrice(Long.valueOf(args[3].trim()));
            call.setContractAddress(args[4].trim());
            call.setMethodName(args[5].trim());
            long naValue = 0L;
            Na na = Na.parseNuls(args[6].trim());
            if (na != null) {
                naValue = na.getValue();
            }
            call.setValue(naValue);

            if(args.length == 9) {
                String argType = args[7].trim();
                if(argType.equals("-d")) {
                    call.setMethodDesc(args[8].trim());
                } else if(argType.equals("-r")) {
                    call.setRemark(args[8].trim());
                } else {
                    return null;
                }
            }else if(args.length == 11) {
                String argType0 = args[7].trim();
                String argType1 = args[9].trim();
                boolean isType0D = argType0.equals("-d");
                boolean isType1D = argType1.equals("-d");
                boolean isType0R = argType0.equals("-r");
                boolean isType1R = argType1.equals("-r");
                if((isType0D && isType1D) || (isType0R && isType1R)) {
                    // 不能同时为-d或-r
                    return null;
                }
                if(isType0D) {
                    call.setMethodDesc(args[8].trim());
                }
                if(isType0R) {
                    call.setRemark(args[8].trim());
                }
                if(isType1D) {
                    call.setMethodDesc(args[10].trim());
                }
                if(isType1R) {
                    call.setRemark(args[10].trim());
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
        ContractCall form = paramsData.get();
        if (null == form) {
            form = getContractCall(args);
        }
        if (null == form) {
            return CommandResult.getFailed("parameter error.");
        }
        String sender = form.getSender();
        RpcClientResult res = CommandHelper.getPassword(sender, restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String) res.getData();

        res = getContractCallArgsJson();
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        Object[] contractArgs = (Object[]) res.getData();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sender", sender);
        parameters.put("gasLimit", form.getGasLimit());
        parameters.put("price", form.getPrice());
        parameters.put("password", password);
        parameters.put("remark", form.getRemark());
        parameters.put("contractAddress", form.getContractAddress());
        parameters.put("value", form.getValue());
        parameters.put("methodName", form.getMethodName());
        parameters.put("methodDesc", form.getMethodDesc());
        parameters.put("args", contractArgs);
        RpcClientResult result = restFul.post("/contract/call", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }


}
