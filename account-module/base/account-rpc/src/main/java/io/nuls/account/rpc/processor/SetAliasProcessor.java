package io.nuls.account.rpc.processor;

import io.nuls.account.model.Address;
import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
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
public class SetAliasProcessor implements CommandProcessor {

    @Override
    public String getCommand() {
        return "setalias";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<alias> The alias of the account, the bytes for the alias is between 3 and 64, - Required")
                .newLine("\t<address> The address of the account, - Required")
                .newLine("\t[password] The password of the account, if the account does not have a password, this entry is not required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "setalias <alias> <address> [password] --Set an alias for the account ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length < 3 || length > 4) {
            return false;
        }
        if (!CommandHelper.checkArgsIsNull(args)) {
            return false;
        }
        if (!StringUtils.validAlias(args[1])) {
            return false;
        }
        if (!Address.validAddress(args[2])) {
            return false;
        }
        if (length == 4 && !StringUtils.validPassword(args[3])) {
            return false;
        }
        return true;
    }

    @Override
    public Result execute(String[] args) {
        return null;
    }
}
