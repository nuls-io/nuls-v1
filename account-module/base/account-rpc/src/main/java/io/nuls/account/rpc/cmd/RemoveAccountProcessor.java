package io.nuls.account.rpc.cmd;

import io.nuls.account.model.Address;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2018/5/25
 */
public class RemoveAccountProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "remove";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> The account address - Required")
                .newLine("\t[password] The password of the account, if the account does not have a password, this entry is not required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "remove <address> [password] --remove an account";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 2 || length > 3) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!Address.validAddress(args[1])) {
            return false;
        }
        if (length == 3 && !StringUtils.validPassword(args[2])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String address = args[1];
        String password = args.length == 3 ? args[2] : null;
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("password", password);
        RpcClientResult result = restFul.post("/account/remove/" + address, parameters);
        if(result.isFailed()){
            return CommandResult.getFailed(result.getMsg());
        }
        return CommandResult.getResult(result);
    }
}
