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
        return "getdepositeds <address> <pageNumber> <pageSize> [agentHash] --Get a list of deposited info based on your account";
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
        if(length == 5 && !StringUtils.validHash(args[4])){
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
        Result result = restFul.get("/consensus/deposit/address/" + address, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
