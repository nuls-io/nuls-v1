package io.nuls.account.rpc.processor;

import io.nuls.account.service.AccountService;
import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Cmd;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;

/**
 * @author: Charlie
 * @date: 2018/5/25
 */
@Cmd
@Component
public class CreateAccountProcessor implements CommandProcessor {

    @Override
    public String getCommand() {
        return "createaccount";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t[password] The password for the account, the password is between 8 and 20 inclusive of numbers and letters, not encrypted by default");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createaccount [password] --create a account, encrypted by [password] | not encrypted by default";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 1 || length > 2) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (length == 2 && !StringUtils.validPassword(args[1])) {
            return false;
        }
        return true;
    }

    @Override
    public Result execute(String[] args) {
        String password = null;
        if(args.length == 2){
            password = args[1];
        }
        // todo
        return null;
    }
}
