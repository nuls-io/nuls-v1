/*
 *
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
 *
 */

package io.nuls.client.processor;

import io.nuls.client.CommandHandler;
import io.nuls.client.constant.CommandConstant;
import io.nuls.client.entity.CommandResult;
import io.nuls.client.helper.CommandBulider;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.SystemService;

/**
 * @author Niels
 * @date 2018/3/7
 */
public abstract class SystemProcessors implements CommandProcessor {

    protected SystemService systemService = SystemService.SYSTEM_SERVICE;

    /**
     * exit the command line
     */
    public static class Exit extends SystemProcessors {

        @Override
        public String getCommand() {
            return "exit";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription());
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "exit --Exit the nuls command line";
        }

        @Override
        public boolean argsValidate(String[] args) {
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            System.exit(1);
            return new CommandResult();
        }
    }

    public static class Help extends SystemProcessors {
        @Override
        public String getCommand() {
            return "help";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t[-a] show all commands and options of command - optional");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "help [-a] --print all commands";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length > 2)
                return false;
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

    public static class Version extends SystemProcessors {
        @Override
        public String getCommand() {
            return "version";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription());
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "version --show the version of local&network";
        }

        @Override
        public boolean argsValidate(String[] args) {
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = systemService.getVersion();
            return CommandResult.getResult(result);
        }
    }
}
