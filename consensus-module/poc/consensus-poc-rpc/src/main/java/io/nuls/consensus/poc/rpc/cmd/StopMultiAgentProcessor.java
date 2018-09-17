package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;

/**
 * @author: tag
 */
public class StopMultiAgentProcessor implements CommandProcessor {
    @Override
    public String getCommand() {
        return "stopMultiAgent";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<address> account address of the agent -required")
                .newLine("\t<signAddress> \tsign address - Required")
                .newLine("\t<pubkey> \t\tPublic key that needs to be signed,If multiple commas are used to separate. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t<m> \t\tAt least how many signatures are required to get the money. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t<txdata> \t\ttransaction data (If it's not a trading promoter  - Required)");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "stopMultiAgent ---If it's a trading promoter <address> <signAddress> <pubkey>,...,<pubkey> <m> " +
                "---Else <address> <signAddress> <txdata>";
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
