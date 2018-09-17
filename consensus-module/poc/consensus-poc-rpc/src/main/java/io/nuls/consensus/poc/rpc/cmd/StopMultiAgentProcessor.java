package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
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
public class StopMultiAgentProcessor implements CommandProcessor {
    private RestFulUtils restFul = RestFulUtils.getInstance();
    @Override
    public String getCommand() {
        return "stopMultiAgent";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> account address of the agent -required")
                .newLine("\t<signAddress> \tsign address - Required")
                .newLine("\t<pubkey> \t\tPublic key that needs to be signed,If multiple commas are used to separate. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t<m> \t\tAt least how many signatures are required to get the money. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t<txdata> \t\ttransaction data (If it's not a trading promoter  - Required)");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "stopMultiAgent ---If it's a trading promoter <address> <signAddress> <pubkey>,...,<pubkey> <m> " +
                "---Else <address> <signAddress> <txdata>";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length != 4 && length != 5){
            return  false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validAddressSimple(args[2])){
            return false;
        }
        if(length == 5){
            if(!StringUtils.validPubkeys(args[3],args[4]))
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
            String[] pubkeys = args[3].split(",");
            parameters.put("pubkeys", Arrays.asList(pubkeys));
            parameters.put("m",Integer.parseInt(args[4]));
        }
        RpcClientResult result = restFul.post("/consensus/agent/stopMutil", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(CommandResult.dataTransformValue(result));
    }
}
