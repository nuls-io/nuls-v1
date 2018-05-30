package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.core.tools.date.DateUtil;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Date;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/28
 */
public class GetConsensusAddressProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getconsensusaddress";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address> agent address -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getconsensusaddress <address> -- get consensus information by agent address";
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
        RpcClientResult result = restFul.get("/consensus/address/" + address,null);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        Map<String, Object> map = (Map)result.getData();
        map.put("reward", CommandHelper.naToNuls(map.get("reward")));
        map.put("rewardOfDay", CommandHelper.naToNuls(map.get("rewardOfDay")));
        map.put("usableBalance", CommandHelper.naToNuls(map.get("usableBalance")));
        map.put("totalDeposit", CommandHelper.naToNuls(map.get("totalDeposit")));
        result.setData(map);
        return CommandResult.getResult(result);
    }
}
