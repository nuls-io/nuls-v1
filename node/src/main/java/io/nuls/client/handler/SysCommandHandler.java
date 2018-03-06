package io.nuls.client.handler;

import io.nuls.client.constant.CommandConstant;
import io.nuls.rpc.sdk.service.SystemService;

public class SysCommandHandler {

    private SystemService systemService = SystemService.getInstance();

    public final static String CMD_EXIT = "exit";

    public final static String CMD_HELP = "help";

    public final static String CMD_VERSION = "version";

    public String processCommand(String[] args) {
        String cmd = args[1];
        switch (cmd) {
            case CMD_EXIT: {
                System.exit(1);
                return "bye";
            }
            case CMD_HELP: {
                return printHelp();
            }
            case CMD_VERSION: {

            }
        }
        StringBuffer buffer = new StringBuffer();
        printSysHelp(buffer);
        return CommandConstant.COMMAND_ERROR + buffer.toString();
    }

    private static String printHelp() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Rpc Command List\n");
        printSysHelp(buffer);
        return buffer.toString();
    }

    private static void printSysHelp(StringBuffer buffer) {
        buffer.append("\n");
        buffer.append("---- sys command ----\n");
        buffer.append("\n");
        buffer.append("sys exit\n");
        buffer.append("sys help                             Get help information\n");
    }

}
