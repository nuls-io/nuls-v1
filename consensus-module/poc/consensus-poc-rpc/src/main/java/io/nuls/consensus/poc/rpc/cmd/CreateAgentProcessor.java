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

package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/28
 */
public class CreateAgentProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "createagent";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<agentAddress>   agent owner address   -required")
                .newLine("\t<packingAddress>    packing address    -required")
                .newLine("\t<commissionRate>    commission rate (10~100), you can have up to 2 valid digits after the decimal point  -required")
                .newLine("\t<deposit>   amount you want to deposit, you can have up to 8 valid digits after the decimal point -required")
                .newLine("\t[rewardAddress]  Billing address    -not required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createagent <agentAddress> <packingAddress> <commissionRate> <deposit> [rewardAddress] --create a agent";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length < 5 || length > 6){
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validAddressSimple(args[2]) || StringUtils.isBlank(args[3])
                || StringUtils.isBlank(args[4])){
            return false;
        }
        if(!StringUtils.isNumberGtZeroLimitTwo(args[3])){
            return false;
        }
        if(!StringUtils.isNuls(args[4])){
            return false;
        }
        if(length == 6 && !StringUtils.validAddressSimple(args[5])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        RpcClientResult res = CommandHelper.getPassword(address, restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = res.isSuccess() ? (String)res.getData() : null;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("agentAddress", address);
        parameters.put("packingAddress", args[2]);
        parameters.put("commissionRate", Double.valueOf(args[3]));
        Long deposit = null;
        try {
            Na na = Na.parseNuls(args[4]);
            deposit = na.getValue();
        } catch (Exception e) {
            return CommandResult.getFailed("Parameter deposit error");
        }
        parameters.put("deposit", deposit);
        parameters.put("password", password);
        if(args.length == 6){
            parameters.put("rewardAddress", args[5]);
        }
        RpcClientResult result = restFul.post("/consensus/agent",parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }
}
