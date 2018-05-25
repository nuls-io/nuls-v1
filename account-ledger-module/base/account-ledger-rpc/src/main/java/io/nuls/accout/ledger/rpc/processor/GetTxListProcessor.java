package io.nuls.accout.ledger.rpc.processor;

import io.nuls.core.tools.cmd.CommandBuilder;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.processor.CommandProcessor;

public class GetTxListProcessor implements CommandProcessor {
    @Override
    public String getCommand() {
        return "gettxlist";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t<address>   address -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "gettxlist <address> --get the transaction information list by address";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 2) {
            return false;
        }
        if (!StringUtils.validAddressSimple(args[1])) {
            return false;
        }
        return true;
    }

    @Override
    public Result execute(String[] args) {
        // todo auto-generated method stub
        return null;
    }
}
