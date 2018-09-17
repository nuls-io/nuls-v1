package io.nuls.consensus.poc.rpc.cmd;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.processor.CommandProcessor;

public class CreateMutilAgentProcessor implements CommandProcessor {
    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String getCommandDescription() {
        return null;
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
