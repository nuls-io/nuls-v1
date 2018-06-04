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
 * @date: 2018/5/28
 */
public class CreateAgentProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "createagent";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<agentAddress>   agent owner address   -required")
                .newLine("\t<packingAddress>    packing address    -required")
                .newLine("\t<commissionRate>    commission rate (10~100)   -required")
                .newLine("\t<deposit>   amount you want to deposit -required")
                .newLine("\t<agentName>  your agent name    -required")
                .newLine("\t<remark>    introduction to your agent -required")
                .newLine("\t[rewardAddress]  Billing address    -not required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createagent <agentAddress> <packingAddress> <commissionRate> <deposit> <agentName> <remark> [rewardAddress] --create a agent";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length < 7 || length > 8){
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validAddressSimple(args[2]) || StringUtils.isBlank(args[3])
                || StringUtils.isBlank(args[4]) || StringUtils.isBlank(args[5]) || StringUtils.isBlank(args[6])){
            return false;
        }
        if(!StringUtils.isNumberGtZero(args[3])){
            return false;
        }
        if(!StringUtils.isNumberGtZero(args[4])){
            return false;
        }
        if(length == 8 && !StringUtils.validAddressSimple(args[7])){
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
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("agentAddress", address);
        parameters.put("packingAddress", args[2]);
        parameters.put("commissionRate", Double.valueOf(args[3]));
        Long deposit = null;
        try {
            Na na = Na.parseNuls(args[4]);
            deposit = na.getValue();
        } catch (Exception e) {
            return CommandResult.getFailed("Parameter deposit error");
        }
        parameters.put("deposit", deposit);
        parameters.put("agentName", args[5]);
        parameters.put("remark", args[6]);
        parameters.put("password", password);
        if(args.length == 8){
            parameters.put("rewardAddress", args[7]);
        }
        RpcClientResult result = restFul.post("/consensus/agent",parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
