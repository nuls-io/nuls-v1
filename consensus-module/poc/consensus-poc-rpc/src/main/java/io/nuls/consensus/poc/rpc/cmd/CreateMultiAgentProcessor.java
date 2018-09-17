package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;

/**
 * @author: tag
 */
public class CreateMultiAgentProcessor implements CommandProcessor {
    @Override
    public String getCommand() {
        return "createMultiAgent";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<agentAddress>   agent owner address   -required")
                .newLine("\t<packingAddress>    packing address    -required")
                .newLine("\t<commissionRate>    commission rate (10~100), you can have up to 2 valid digits after the decimal point  -required")
                .newLine("\t<deposit>   amount you want to deposit, you can have up to 8 valid digits after the decimal point -required")
                .newLine("\t<signAddress> \tsign address - Required")
                .newLine("\t<pubkey>,...<pubkey> \tPublic key that needs to be signed,If multiple commas are used to separate.")
                .newLine("\t<m> \tAt least how many signatures are required to get the money.")
                .newLine("\t[rewardAddress]  Billing address    -not required")
                .newLine("\t<txdata> \tExit consensus transaction data currently created");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "createMultiAgent --- If it's a trading promoter <agentAddress> <packingAddress> <commissionRate> <deposit> <signAddress> <pubkey>,...<pubkey> <m> [rewardAddress]" +
                "\t              --- Else <address> <signAddress> <txdata>";
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
