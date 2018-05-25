package io.nuls.account.rpc.processor;

import io.nuls.account.model.Address;
import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;

/**
 * @author: Charlie
 * @date: 2018/5/25
 */
public class SetPasswordProcessor implements CommandProcessor {


    @Override
    public String getCommand() {
        return "setpwd";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> address of the account - Required")
                .newLine("\t<password> account password (8-20 characters, letters and numbers) - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "setpwd <address> <password> --set password for the account";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 3) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!Address.validAddress(args[1])) {
            return false;
        }
        if (!StringUtils.validPassword(args[2])) {
            return false;
        }
        String newPwd = args[2];
        CommandHelper.confirmPwd(newPwd);
        return true;
    }

    @Override
    public Result execute(String[] args) {
        return null;
    }
}
