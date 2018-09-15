package io.nuls.accout.ledger.rpc.cmd;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;

public class TransferP2shProcess implements CommandProcessor {
    @Override
    public String getCommand() {
        return "transferP2sh";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<signAddress> \tsign address - Required")
                .newLine("\t[address] \t\ttransfer address(If it's a trading promoter - Required; else - Not Required) ")
                .newLine("\t[toAddress],[toamount];....;[toAddress][toamount] \tThe meaning of [toAddress],[toamount] is pay  toAddress toamount nuls," +
                        "Separate multiple [toAddress],[toamount],If there are multiple payee Separate multiple. " +
                        "(If it's a trading promoter - Required; else - Not Required) toamount must greater than 0")
                .newLine("\t[amount] \t\tamount, you can have up to 8 valid digits after the decimal point(If it's a trading promoter - Required; else - Not Required) - Required")
                .newLine("\t[pubkey] \t\tPublic key that needs to be signed,If multiple commas are used to separate. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t[m] \t\tAt least how many signatures are required to get the money. (If it's a trading promoter - Required; else - Not Required)")
                .newLine("\t[txdata] \t\ttransaction data (If it's not a trading promoter  - Required)")
                .newLine("\t[remark] \t\tremark - Not Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "transferP2sh [address] <signAddress> [toAddress],[toamount];....;[toAddress][toamount]" +
                "[pubkey],...[pubkey] [m] [txdata] [amount]";
    }

    @Override
    public boolean argsValidate(String[] args) {
        boolean result = false;
        int length = args.length;
        //length=2表示不是第一个签名者
        if(length == 2){

        }else{

        }
        return result;
    }

    @Override
    public CommandResult execute(String[] args) {
        return null;
    }
}
