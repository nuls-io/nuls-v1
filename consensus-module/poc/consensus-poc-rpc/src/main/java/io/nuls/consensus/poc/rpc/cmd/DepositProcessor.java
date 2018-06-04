package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/29
 */
public class DepositProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "deposit";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address>   Your own account address -required")
                .newLine("\t<agentHash>   The agent hash you want to deposit  -required")
                .newLine("\t<deposit>   the amount you want to deposit -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "deposit <address> <agentHash> <deposit> --apply for deposit";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 4){
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validHash(args[2])
                || StringUtils.isBlank(args[3]) || !StringUtils.isNumberGtZero(args[3])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        RpcClientResult res = CommandHelper.getPassword(address, restFul);
        if(res.isFailed() && !res.getCode().equals(KernelErrorCode.SUCCESS.getCode())){
            return CommandResult.getFailed(res.getMsg());
        }
        String password = (String)res.getData();
        Long amount = Na.parseNuls(args[3]).getValue();
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("address", address);
        parameters.put("agentHash", args[2]);
        parameters.put("deposit", amount);
        parameters.put("password", password);
        RpcClientResult result = restFul.post("/consensus/deposit", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
