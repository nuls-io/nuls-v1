/*
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

package io.nuls.client.cmd;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.CommandBuilder;
import io.nuls.kernel.utils.RestFulUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author: Charlie
 */
public class UpgradeProcessor implements CommandProcessor {

    private RestFulUtils restFul = RestFulUtils.getInstance();

    @Override
    public String getCommand() {
        return "upgrade";
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine(getCommandDescription()).newLine("\t<version>  the version you want to upgrade -required");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "upgrade <version> --upgrade to the newest version and restart the client where download complete";
    }

    @Override
    public boolean argsValidate(String[] args) {
        if (args.length == 2 && args[1] != null && args[1].trim().length() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public CommandResult execute(String[] args) {
        RpcClientResult result = restFul.post("/client/upgrade/" + args[1], "");
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        //todo 打印进度，完成时确认重启
        int count = 0;
        while (true) {
            result = restFul.get("/client/upgrade", null);
            if (result.isFailed()) {
                return CommandResult.getFailed(result);
            }
            int percentage = (int) ((Map) result.getData()).get("percentage");
            //todo 打印
            print(percentage + "%", count);
            if (percentage < 10) {
                count = 2;
            } else {
                count = 3;
            }

            if (percentage == 100) {
                break;
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                Log.error(e.getMessage());
            }
        }
        result = restFul.post("/client/restart", "");

        return CommandResult.getResult(result);
    }

    private void print(String s, int backCount) {
        try {
            for (int i = 0; i < backCount; i++) {
//todo            CommandHandler.CONSOLE_READER.backspace();
//            CommandHandler.CONSOLE_READER.delete();
            }
            CommandHandler.CONSOLE_READER.clearScreen();
            CommandHandler.CONSOLE_READER.print(s);
            CommandHandler.CONSOLE_READER.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
