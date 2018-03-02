/**
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.client;

import io.nuls.client.constant.CommandConstant;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Niels on 2017/10/26.
 */
public class CommandHandler {

    private static CommandHandler instance = new CommandHandler();

    public static void main(String[] args) throws IOException {
        System.out.print(CommandConstant.COMMAND_PS1);
        Scanner scan = new Scanner(System.in);
        while (scan.hasNext()) {
            String read = scan.nextLine().trim();
            if (StringUtils.isBlank(read)) {
                System.out.print(CommandConstant.COMMAND_PS1);
                continue;
            }
            System.out.print(instance.processCommand(read.split(" ")));
            System.out.print(CommandConstant.COMMAND_PS1);
        }
    }

    private String processCommand(String[] args) {
        if(args.length == 1) {
            return CommandConstant.COMMAND_ERROR;
        }
        String command = args[0];

        switch (command) {
            case CommandConstant.CMD_SYS:
                return processSys(args);
            case CommandConstant.CMD_ACCT:
                return "";
        }
        return "command error";
    }

    private String processSys(String[] args) {
        String command = args[1];
        switch (command) {
            case CommandConstant.CMD_EXIT:
                return stop();
            case CommandConstant.CMD_HELP:
                return printHelp();
        }
        return "command error";
    }

    private String printHelp() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Rpc Command List\n");
        printSysHelp(buffer);
        return buffer.toString();
    }

    private void printSysHelp(StringBuffer buffer) {
        buffer.append("\n");
        buffer.append("---- sys command ----\n");
        buffer.append("\n");
        buffer.append("sys exit\n");
        buffer.append("sys help                              Get help information\n");
    }

    private String stop() {
        Log.debug("stoping...");
        System.exit(0);
        return "bye!";
    }
}
