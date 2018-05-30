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
