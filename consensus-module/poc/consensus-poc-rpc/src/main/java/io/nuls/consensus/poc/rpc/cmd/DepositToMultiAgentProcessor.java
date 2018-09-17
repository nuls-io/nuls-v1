package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: tag
 */
public class DepositToMultiAgentProcessor implements CommandProcessor {
    private RestFulUtils restFul = RestFulUtils.getInstance();
    @Override
    public String getCommand() {
        return "depositToMultiAgent";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<signAddress> \tsign address - Required")
                .newLine("\t<address>   Your own account address -required")
                .newLine("\t<agentHash>   The agent hash you want to deposit  -required")
                .newLine("\t<deposit>   the amount you want to deposit, you can have up to 8 valid digits after the decimal point -required")
                .newLine("\t<pubkey> \t\tPublic key that needs to be signed,If multiple commas are used to separate. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t<m> \t\tAt least how many signatures are required to get the money. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t<txdata> \t\ttransaction data (If it's not a trading promoter  - Required)");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "depositToMultiAgent --- If it's a trading promoter <signAddress>  <address> <agentHash> <deposit> <pubkey>,...<pubkey> <m>" +
                "\t                 --- Else <address> <signAddress> <txdata>";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 4 && length != 7){
            return  false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validAddressSimple(args[2])){
            return false;
        }
        if(length == 7){
            if(!NulsDigestData.validHash(args[3]) || StringUtils.isBlank(args[4]) || !StringUtils.isNuls(args[4]))
                return  false;
            if(!StringUtils.validPubkeys(args[5],args[6]))
                return  false;
        }else{
            if(args[3] == null || args[3].length() == 0)
                return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String signAddress = args[2];
        RpcClientResult res = CommandHelper.getPassword(signAddress, restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String)res.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", args[1]);
        parameters.put("signAddress", args[2]);
        if(args.length == 4){
            parameters.put("txdata",args[3]);
        }else{
            Long amount = Na.parseNuls(args[4]).getValue();
            parameters.put("agentHash", args[3]);
            parameters.put("deposit", amount);
            parameters.put("password", password);
            String[] pubkeys = args[3].split(",");
            parameters.put("pubkeys", Arrays.asList(pubkeys));
            parameters.put("m",Integer.parseInt(args[5]));
            parameters.put("txHash",Integer.parseInt(args[6]));
        }
        RpcClientResult result = restFul.post("/consensus/mutilDeposit", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(CommandResult.dataTransformValue(result));
    }
}
