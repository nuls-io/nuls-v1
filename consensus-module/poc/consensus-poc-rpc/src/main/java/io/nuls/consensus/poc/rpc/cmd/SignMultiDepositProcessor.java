package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

public class SignMultiDepositProcessor implements CommandProcessor {
    private RestFulUtils restFul = RestFulUtils.getInstance();
    @Override
    public String getCommand() {
        return "signMultiTransfer";
    }

    @Override
    public String getHelp() {
        return "signMultiTransfer <signAddress> <txdata> -sign a multiTransfer";
    }

    @Override
    public String getCommandDescription() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> \t\tsource address - Required")
                .newLine("\t<txdata> \t\ttransaction data - Required");
        return builder.toString();
    }

    @Override
    public boolean argsValidate(String[] args) {
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        return  StringUtils.validSign(args);
    }

    @Override
    public CommandResult execute(String[] args) {
        RpcClientResult res = CommandHelper.getPassword(args[2], restFul);
        if(!res.isSuccess()){
            return CommandResult.getFailed(res);
        }
        String password = (String)res.getData();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("signAddress",args[1]);
        parameters.put("txdata",args[2]);
        parameters.put("password",password);
        RpcClientResult result = restFul.post("/multiAccount/signMultiDeposit", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(CommandResult.dataTransformValue(result));

    }
}
