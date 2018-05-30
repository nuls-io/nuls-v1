package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/29
 */
public class WithdrawProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "withdraw";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address>   address -required")
                .newLine("\t<txHash>    your deposit transaction hash  -required")
                .newLine("\t[password]  The password of the account, if the account does not have a password, this entry is not required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "withdraw <address> <txHash> [password] -- withdraw the agent";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length < 3 || length > 4){
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if(!StringUtils.validAddressSimple(args[1]) || !StringUtils.validHash(args[2])){
            return false;
        }
        if(length == 4 && !StringUtils.validPassword(args[3])){
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String password = args.length == 4 ? args[3] : null;
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("address", args[1]);
        parameters.put("txHash", args[2]);
        parameters.put("password", password);
        Result result = restFul.post("/consensus/withdraw", parameters);
        if (result.isFailed()) {
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
