package io.nuls.account.rpc.processor;

import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.cmd.CommandHelper;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;

public class CreateAccountProcessor implements CommandProcessor {
    @Override
    public String getCommand() {
        return "createaccount";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();

        builder.newLine(getCommandDescription())
                .newLine("\t<password> the password your wallet, Required")
                .newLine("\t<count> the count of account you want to create, Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createaccount <password> <count> --create <count> accounts , encrypted by <password>";
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
        // validate <count>
        if (!StringUtils.isNumeric(args[2])) {
            return false;
        }
        return false;
    }

    @Override
    public Result execute(String[] args) {
        // todo auto-generated method stub
        return null;
    }
}
