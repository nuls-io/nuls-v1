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
public class CreateMultiAgentProcessor implements CommandProcessor {
    private RestFulUtils restFul = RestFulUtils.getInstance();
    @Override
    public String getCommand() {
        return "createMultiAgent";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<agentAddress>   agent owner address   -required")
                .newLine("\t<packingAddress>    packing address    -required")
                .newLine("\t<commissionRate>    commission rate (10~100), you can have up to 2 valid digits after the decimal point  -required")
                .newLine("\t<deposit>   amount you want to deposit, you can have up to 8 valid digits after the decimal point -required")
                .newLine("\t<signAddress> \tsign address - Required")
                .newLine("\t<pubkey>,...<pubkey> \tPublic key that needs to be signed,If multiple commas are used to separate.")
                .newLine("\t<m> \tAt least how many signatures are required to get the money.")
                .newLine("\t[rewardAddress]  Billing address    -not required")
                .newLine("\t<txdata> \tExit consensus transaction data currently created");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createMultiAgent --- If it's a trading promoter <agentAddress> <signAddress> <packingAddress> <commissionRate> <deposit> <pubkey>,...<pubkey> <m> [rewardAddress]" +
                "\t              --- Else <address> <signAddress> <txdata>";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 4 && length != 9 && length != 8){
            return  false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validAddressSimple(args[2])){
            return false;
        }
        if(length == 9 || length == 8){
            if(!StringUtils.validAddressSimple(args[3]) || StringUtils.isBlank(args[4]) || StringUtils.isBlank(args[5]))
                return  false;
            if(!StringUtils.isNumberGtZeroLimitTwo(args[4])){
                return false;
            }
            if(!StringUtils.isNuls(args[5])){
                return false;
            }
            if(!StringUtils.validPubkeys(args[6],args[7]))
                return  false;
            if(length == 9 && !StringUtils.validAddressSimple(args[8])){
                return false;
            }
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
        parameters.put("agentAddress", args[1]);
        parameters.put("signAddress", args[2]);
        if(args.length == 4){
            parameters.put("txdata",args[3]);
        }else{
            parameters.put("packingAddress", args[3]);
            parameters.put("commissionRate", Double.valueOf(args[4]));
            Long deposit = null;
            try {
                Na na = Na.parseNuls(args[5]);
                deposit = na.getValue();
            } catch (Exception e) {
                return CommandResult.getFailed("Parameter deposit error");
            }
            parameters.put("deposit", deposit);
            parameters.put("password", password);
            String[] pubkeys = args[6].split(",");
            parameters.put("pubkeys", Arrays.asList(pubkeys));
            parameters.put("m",Integer.parseInt(args[7]));
            if(args.length == 9){
                parameters.put("rewardAddress", args[8]);
            }        }
        RpcClientResult result = restFul.post("/consensus/mutilAgent", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(CommandResult.dataTransformValue(result));
    }
}
