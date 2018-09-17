package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;

/**
 * @author: tag
 */
public class WithdrawMultiProcessor implements CommandProcessor {
    @Override
    public String getCommand() {
        return "withdrawMulti";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> \ttransfer address - Required")
                .newLine("\t<signAddress> \tsign address - Required")
                .newLine("\t<pubkey>,...<pubkey> \tPublic key that needs to be signed,If multiple commas are used to separate.")
                .newLine("\t<m> \tAt least how many signatures are required to get the money.")
                .newLine("\t<txhash> \tCurrent consensus transaction hash")
                .newLine("\t<txdata> \tExit consensus transaction data currently created")
                .newLine("\t[remark] \tremark - Not Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "withdrawMulti --- If it's a trading promoter <address> <signAddress> <pubkey>,...<pubkey> <m> <txhash> [remark]" +
                "\t           --- Else <address> <signAddress> <txdata>";
    }

    @Override
    public boolean argsValidate(String[] args) {
        return false;
    }

    @Override
    public CommandResult execute(String[] args) {
        return null;
    }
}
