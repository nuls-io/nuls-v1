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
package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsDigestData;
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
public class CreateMultiDepositProcessor implements CommandProcessor {


    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "createMultiDeposit";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address>   Your own account address -required")
                .newLine("\t<signAddress> \tsign address - Required")
                .newLine("\t<agentHash>   The agent hash you want to deposit  -required")
                .newLine("\t<deposit>   the amount you want to deposit, you can have up to 8 valid digits after the decimal point -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createMultiDeposit <address> <signAddress> <agentHash> <deposit> --apply for multi deposit";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 5){
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validAddressSimple(args[2])  || !NulsDigestData.validHash(args[3])
                || StringUtils.isBlank(args[4]) || !StringUtils.isNuls(args[4])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String signAddress = args[2];
        RpcClientResult res = CommandHelper.getPassword(signAddress, restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String)res.getData();
        Long amount = Na.parseNuls(args[4]).getValue();
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("address", args[1]);
        parameters.put("signAddress", args[2]);
        parameters.put("agentHash", args[3]);
        parameters.put("deposit", amount);
        parameters.put("password", password);
        RpcClientResult result = restFul.post("/consensus/multiAccount/createMultiDeposit", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(CommandResult.dataMultiTransformValue(result));
    }
}
