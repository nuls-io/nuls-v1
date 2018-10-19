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

import io.nuls.core.tools.date.DateUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * query contract transactions by contract address
 * Created by wangkun23 on 2018/9/20.
 */
public class GetContractTxListProcessor implements CommandProcessor {

    private RestFulUtils restFulUtils = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getcontracttxlist";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address>      address -required")
                .newLine("\t<<account>>    account -required")
                .newLine("\t<pageNumber>   pageNumber -required")
                .newLine("\t<pageSize>     pageSize -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getcontracttxlist <address> <account> <pageNumber> <pageSize> --get the contract transactions by address";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 4 || length > 5) {
            return false;
        }
        if (args.length == 4) {
            if (!StringUtils.isNumeric(args[2]) || !StringUtils.isNumeric(args[3])) {
                return false;
            }
        } else {
            if (!StringUtils.isNumeric(args[3]) || !StringUtils.isNumeric(args[4])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        int pageNumber = 0;
        int pageSize = 0;
        if (args.length == 4) {
            pageNumber = Integer.parseInt(args[2]);
            pageSize = Integer.parseInt(args[3]);
        } else {
            pageNumber = Integer.parseInt(args[3]);
            pageSize = Integer.parseInt(args[4]);
        }
        String address = args[1];
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pageNumber", pageNumber);
        parameters.put("pageSize", pageSize);

        /**
         * user type accountAddress argument.
         */
        if (args.length == 5) {
            parameters.put("accountAddress", args[2]);
        }
        String url = "/contract/tx/list/" + address;
        RpcClientResult result = restFulUtils.get(url, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        /**
         * format amount and trx fee. 1NULS = 100000000Na
         */
        List<Map<String, Object>> list = (List<Map<String, Object>>) ((Map) result.getData()).get("list");
        for(Map<String, Object> map : list){
            map.put("fee", CommandHelper.naToNuls(map.get("fee")));
            map.put("value", CommandHelper.naToNuls(map.get("value")));
            map.put("time",  DateUtil.convertDate(new Date((Long)map.get("time"))));
            //map.put("status", statusExplain((Integer)map.get("status")));
            map.put("type", CommandHelper.txTypeExplain((Integer)map.get("type")));

            List<Map<String, Object>> inputs = (List<Map<String, Object>>)map.get("inputs");
            for(Map<String, Object> input : inputs){
                input.put("value", CommandHelper.naToNuls(input.get("value")));
            }
            map.put("inputs", inputs);
            List<Map<String, Object>> outputs = (List<Map<String, Object>>)map.get("outputs");
            for(Map<String, Object> output : outputs){
                output.put("value", CommandHelper.naToNuls(output.get("value")));
                //output.put("status", statusExplainForOutPut((Integer) output.get("status")));
            }
            map.put("outputs", outputs);

            Map<String, Object> txDataMap = (Map) map.get("txData");
            if(txDataMap != null) {
                Map<String, Object> dataMap = (Map) txDataMap.get("data");
                if(dataMap != null) {
                    dataMap.put("value", CommandHelper.naToNuls(dataMap.get("value")));
                    dataMap.put("price", CommandHelper.naToNuls(dataMap.get("price")));
                }
            }

            Map<String, Object> contractResultMap = (Map) map.get("contractResult");
            if(contractResultMap != null) {
                contractResultMap.put("totalFee", CommandHelper.naToNuls(contractResultMap.get("totalFee")));
                contractResultMap.put("txSizeFee", CommandHelper.naToNuls(contractResultMap.get("txSizeFee")));
                contractResultMap.put("actualContractFee", CommandHelper.naToNuls(contractResultMap.get("actualContractFee")));
                contractResultMap.put("refundFee", CommandHelper.naToNuls(contractResultMap.get("refundFee")));
                contractResultMap.put("value", CommandHelper.naToNuls(contractResultMap.get("value")));
                contractResultMap.put("price", CommandHelper.naToNuls(contractResultMap.get("price")));
                contractResultMap.put("balance", CommandHelper.naToNuls(contractResultMap.get("balance")));
            }
        }
        result.setData(list);
        return CommandResult.getResult(result);
    }
}
