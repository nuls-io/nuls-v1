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
