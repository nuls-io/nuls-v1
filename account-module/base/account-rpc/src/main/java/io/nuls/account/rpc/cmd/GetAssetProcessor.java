package io.nuls.account.rpc.cmd;

import io.nuls.account.model.Address;
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
public class GetAssetProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getasset";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> address - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return  "getasset <address> --get your assets";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 2) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!Address.validAddress(args[1])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        RpcClientResult result = restFul.get("/account/assets/" + address, null);
        if(result.isFailed()){
            return CommandResult.getFailed(result.getMsg());
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>)((Map)result.getData()).get("list");
        for(Map<String, Object> map : list){
            map.put("balance",  CommandHelper.naToNuls(map.get("balance")));
            map.put("usable", CommandHelper.naToNuls(map.get("usable")));
            map.put("locked", CommandHelper.naToNuls(map.get("locked")));
        }
        result.setData(list);
        return CommandResult.getResult(result);
    }
}
