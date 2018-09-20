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

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.RestFulUtils;

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
                .newLine("\t<pageNumber>   pageNumber -required")
                .newLine("\t<pageSize>     pageSize -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getcontracttxlist address <account> <pageNumber> <pageSize> --get the contract transactions by address";
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

        RpcClientResult result = restFulUtils.get("/contract/tx/list/" + address, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        /**
         * TODO.. format amount and trx fee.
         */
        //List<Map<String, Object>> list = (List<Map<String, Object>>) ((Map) result.getData()).get("list");
        //result.setData(list);
        return CommandResult.getResult(result);
    }
}
