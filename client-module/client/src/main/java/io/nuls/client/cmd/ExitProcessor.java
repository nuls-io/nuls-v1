package io.nuls.client.cmd;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;

/**
 * @author: Charlie
 * @date: 2018/5/30
 */
public class ExitProcessor implements CommandProcessor {

    @Override
    public String getCommand() {
        return "exit";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription());
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "exit --exit the nuls command";
    }

    @Override
    public boolean argsValidate(String[] args) {
        if(args.length > 1) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        System.exit(1);
        return CommandResult.getSuccess("");
    }
}
