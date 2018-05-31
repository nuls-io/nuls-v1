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
 * @date: 2018/5/25
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
            return CommandResult.getFailed(result.getMsg());
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>)((Map)result.getData()).get("list");
        for(Map<String, Object> map : list){
            map.put("createTime",  DateUtil.convertDate(new Date((Long)map.get("createTime"))));
        }
        result.setData(list);
        return CommandResult.getResult(result);
    }
}
