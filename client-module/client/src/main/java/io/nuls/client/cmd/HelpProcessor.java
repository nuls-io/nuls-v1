package io.nuls.client.cmd;

import io.nuls.client.constant.CommandConstant;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.RestFulUtils;

/**
 * @author: Charlie
 * @date: 2018/5/30
 */
public class HelpProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription())
                .newLine("\t[-a] show all commands and options of command - optional");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "help [-a] --print all commands";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if(length > 2) {
            return false;
        }
        if(length == 2 && !CommandConstant.NEED_ALL.equals(args[1])) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        int length = args.length;
        StringBuilder str = new StringBuilder();
        str.append("all commands:");
        for (CommandProcessor processor : CommandHandler.PROCESSOR_MAP.values()) {
            str.append("\n");
            if(length == 2 && CommandConstant.NEED_ALL.equals(args[1])) {
                str.append(processor.getHelp());
            } else {
                str.append(processor.getCommandDescription());
            }

        }
        return CommandResult.getSuccess(str.toString());
    }
}
