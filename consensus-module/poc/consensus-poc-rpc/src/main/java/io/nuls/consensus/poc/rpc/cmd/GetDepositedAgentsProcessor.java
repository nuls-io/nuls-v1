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
 * 根据地址查询该账户委托的节点列表(返回节点信息列表)
 * Get a list of deposited agent info based on your account
 * @author: Charlie
 * @date: 2018/5/29
 */
public class GetDepositedAgentsProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "getdepositedagents";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address> address - Required")
                .newLine("\t<pageNumber> pageNumber - Required")
                .newLine("\t<pageSize> pageSize(1~100) - Required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getdepositedagents <address> <pageNumber> <pageSize> --Get a list of deposited agent info based on your account";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 4) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!StringUtils.validAddressSimple(args[1]) || !StringUtils.isNumeric(args[2]) || !StringUtils.isNumeric(args[3])) {
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
        Result result = restFul.get("/consensus/agent/address/" + address, parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
