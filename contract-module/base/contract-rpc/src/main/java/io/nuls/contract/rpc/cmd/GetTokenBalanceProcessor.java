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

import java.util.Map;

import static io.nuls.contract.util.ContractUtil.valueOf;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/9/22
 */
public class GetTokenBalanceProcessor implements CommandProcessor {
    /**
     * rest utils
     */
    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "gettokenbalance";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<contractAddress> contract address -required")
                .newLine("\t<address> account address -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "gettokenbalance <contractAddress> <address> --get the token balance";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 3) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String contractAddress = args[1].trim();
        String address = args[2].trim();
        String url = "/contract/balance/token/" + contractAddress + "/" + address;
        RpcClientResult result = restFul.get(url, null);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        Map<String, Object> data = (Map) result.getData();
        data.put("amount", CommandHelper.tokenRecovery(valueOf(data.get("amount")), (Integer) data.get("decimals")));
        data.put("status", statusExplain((Integer) data.get("status")));
        data.remove("decimals");
        data.remove("blockHeight");
        return CommandResult.getResult(result);
    }

    private String statusExplain(Integer status){
        if(status == 0){
            return "none";
        }
        if(status == 1){
            return"normal";
        }
        if(status == 2){
            return"termination";
        }
        return "unknown";
    }
}
