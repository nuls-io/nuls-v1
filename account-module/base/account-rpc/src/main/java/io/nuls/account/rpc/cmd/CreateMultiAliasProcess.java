package io.nuls.account.rpc.cmd;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: tag
 */
public class CreateMultiAliasProcess implements CommandProcessor {
    private RestFulUtils restFul = RestFulUtils.getInstance();
    @Override
    public String getCommand() {
        return "setMultiAlias";
    }

    @Override
    public String getHelp() {
        return "setMultiAlias <address> <alias> <signAddress>  --Set an alias for the multi account ";
    }

    @Override
    public String getCommandDescription() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> The address of the account, - Required")
                .newLine("\t<alias> The alias of the account, the bytes for the alias is between 1 and 20 " +
                        "(only lower case letters, Numbers and underline, the underline should not be at the begin and end), - Required")
                .newLine("\t<signAddress> \tsign address address - Required");
        return builder.toString();
    }

    @Override
    public boolean argsValidate(String[] args) {
        if(args.length != 4){
            return  false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (StringUtils.isBlank(args[1])||StringUtils.isBlank(args[3])) {
            return false;
        }
        if (!StringUtils.validAlias(args[2])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String signAddress = args[3];
        RpcClientResult res = CommandHelper.getPassword(signAddress, restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String)res.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("address", args[1]);
        parameters.put("alias", args[2]);
        parameters.put("signAddress", args[3]);
        parameters.put("password", password);
        RpcClientResult result = restFul.post("/multiAccount/mutilAlias", parameters);
        if(result.isFailed()){
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(CommandResult.dataTransformValue(result));
    }
}
