package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 根据账户地址获取该账户参与的所有委托(共识)信息的统计
 * According to the account address to obtain all information on the deposit of the account
 * @author: Charlie
 * @date: 2018/5/29
 */
public class GetDepositedInfoProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getdepositedinfo";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address> address of the account - Required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getdepositedinfo <address>  --According to the account address to obtain overview on the deposited of the account";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 2){
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.validAddressSimple(args[1])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        Result result = restFul.get("/agent/address/" + address, null);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
