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

package io.nuls.account.rpc.cmd;

import io.nuls.core.tools.date.DateUtil;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/25
 */
public class GetWalletBalanceProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getwalletbalance";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription());
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getwalletbalance --get total balance of all account in the wallet";
    }

    @Override
    public boolean argsValidate(String[] args) {
        if(args.length > 1) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        RpcClientResult result = restFul.get("/account/balance", null);
        if(result.isFailed()){
            return CommandResult.getFailed(result.getMsg());
        }
        Map<String, Object> map = (Map)result.getData();
        map.put("balance",  CommandHelper.naToNuls(map.get("balance")));
        map.put("usable", CommandHelper.naToNuls(map.get("usable")));
        map.put("locked", CommandHelper.naToNuls(map.get("locked")));
        result.setData(map);
        return CommandResult.getResult(result);
    }
}
