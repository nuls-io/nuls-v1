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

package io.nuls.account.rpc.cmd;

import io.nuls.core.tools.date.DateUtil;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 */
public class GetAccountsProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getaccounts";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<pageNumber>   pageNumber -required")
                .newLine("\t<pageSize>     pageSize -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getaccounts <pageNumber> <pageSize> --get all account info list int the wallet";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 3) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.isNumeric(args[1]) || !StringUtils.isNumeric(args[2])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        int pageNumber = Integer.parseInt(args[1]);
        int pageSize = Integer.parseInt(args[2]);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pageNumber", pageNumber);
        parameters.put("pageSize", pageSize);
        RpcClientResult result = restFul.get("/account", parameters);
        if(result.isFailed()){
            return CommandResult.getFailed(result);
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>)((Map)result.getData()).get("list");
        for(Map<String, Object> map : list){
            map.put("createTime",  DateUtil.convertDate(new Date((Long)map.get("createTime"))));
        }
        result.setData(list);
        return CommandResult.getResult(result);
    }
}
