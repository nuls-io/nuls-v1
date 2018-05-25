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
public class RemoveAccountProcessor implements CommandProcessor {

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
        if (!StringUtils.validPassword(args[2])) {
            return false;
        }
        return true;
    }

    @Override
    public RpcClientResult execute(String[] args) {
        return null;
    }
}
