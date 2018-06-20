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

import io.nuls.core.tools.date.DateUtil;
import io.nuls.kernel.model.NulsDigestData;
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
 * 根据地址获取该账户参与的委托信息列表(返回共识信息列表)
 * Get a list of deposited info based on your account
 *
 * @author: Charlie
 * @date: 2018/5/29
 */
public class GetDepositedsProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getdepositeds";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address> address - Required")
                .newLine("\t<pageNumber> pageNumber - Required")
                .newLine("\t<pageSize> pageSize(1~100) - Required")
                .newLine("\t[agentHash] the agent node hash (default query all)");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getdepositeds <address> <pageNumber> <pageSize> [agentHash] --get a list of deposited info based on your account";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length < 4 || length > 5) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }

        if (!StringUtils.validAddressSimple(args[1]) || !StringUtils.isNumeric(args[2]) || !StringUtils.isNumeric(args[3])) {
            return false;
        }
        if(length == 5 && !NulsDigestData.validHash(args[4])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        int pageNumber = Integer.parseInt(args[2]);
        int pageSize = Integer.parseInt(args[3]);
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("pageNumber", pageNumber);
        parameters.put("pageSize", pageSize);
        if(args.length == 5){
            parameters.put("agentHash", args[4]);
        }
        RpcClientResult result = restFul.get("/consensus/deposit/address/" + address, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>)((Map)result.getData()).get("list");
        for(Map<String, Object> map : list){
            map.put("deposit",  CommandHelper.naToNuls(map.get("deposit")));
            map.put("status", CommandHelper.consensusExplain((Integer) map.get("status")));
            map.put("time",  DateUtil.convertDate(new Date((Long)map.get("time"))));
        }
        result.setData(list);
        return CommandResult.getResult(result);
    }
}
