package io.nuls.client;

import io.nuls.client.constant.CommandConstant;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Niels on 2017/10/26.
 */
public class CommandHandle {
    public static void main(String[] args) throws IOException {
        System.out.print(CommandConstant.COMMAND_PS1);
        Scanner scan = new Scanner(System.in);
        while (scan.hasNext()) {
            String read = scan.nextLine();
            doCommand(read);
            System.out.print(CommandConstant.COMMAND_PS1);
        }
    }

    private static void doCommand(String read) {
        if (StringUtils.isBlank(read)) {
            return;
        }
        read = read.trim();
        switch (read) {
            case CommandConstant.CMD_EXIT:
                doStop();
                break;
            case CommandConstant.CMD_HELP:
                printHelp();
                break;
            default:
                break;
        }
    }

    private static void printHelp() {
        //todo
    }

    private static void doStop() {
        Log.debug("stoping...");
        System.exit(0);
    }
}
