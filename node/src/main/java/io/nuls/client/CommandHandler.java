/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
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
import io.nuls.client.handler.SysCommandHandler;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author Niels
 * @date 2017/10/26
 */
public class CommandHandler {

    private SysCommandHandler sysHandler;

    private void init() {
        sysHandler = new SysCommandHandler();
    }

    public static void main(String[] args) throws IOException {
        CommandHandler instance = new CommandHandler();
        instance.init();
        System.out.print(CommandConstant.COMMAND_PS1);
        Scanner scan = new Scanner(System.in);
        while (scan.hasNextLine()) {
            String read = scan.nextLine().trim();
            if (StringUtils.isBlank(read)) {
                System.out.print(CommandConstant.COMMAND_PS1);
                continue;
            }
            System.out.println(CommandConstant.COMMAND_PS1 + instance.processCommand(read.split(" ")));
            System.out.print(CommandConstant.COMMAND_PS1);
        }
    }

    private String processCommand(String[] args) {
        if (args.length == 1) {
            return CommandConstant.COMMAND_ERROR;
        }
        String command = args[0];
        switch (command) {
            case CommandConstant.CMD_SYS:
                return sysHandler.processCommand(args);
            case CommandConstant.CMD_ACCT:
                return "";
        }
        return "command error";
    }

    private String stop() {
        Log.debug("stoping...");
        System.exit(0);
        return "bye!";
    }
}
