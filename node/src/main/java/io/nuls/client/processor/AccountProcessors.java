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

import io.nuls.client.entity.CommandResult;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.utils.param.AssertUtil;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.AccountService;

/**
 * @author Niels
 * @date 2018/3/7
 */
public abstract class AccountProcessors implements CommandProcessor {

    protected AccountService accountService = new AccountService();

    /**
     * create accounts processor
     */
    public static class CreateAccount extends AccountProcessors {

        @Override
        public String getCommand() {
            return "createaccount";
        }

        @Override
        public String getCommandDescription() {
            return "createaccount <password> <count> --create <count> accounts & Encrypting with with <password>";
        }

        @Override
        public boolean argsValidate(String[] args) {
            if (args.length != 3) {
                return false;
            }
            AssertUtil.canNotEmpty(args[1]);
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.create(args[1], Integer.parseInt(args[2]));
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }


    /**
     * get the balance of a address
     */
    public static class GetBalance extends AccountProcessors {

        @Override
        public String getCommand() {
            return "balance";
        }

        @Override
        public String getCommandDescription() {
            return "balance <address> --get the balance of a address";
        }

        @Override
        public boolean argsValidate(String[] args) {
            if (args.length != 2) {
                return false;
            }
            AssertUtil.canNotEmpty(args[1]);
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getBalance(args[1]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }


    /**
     * get the balance of a address
     */
    public static class ImportAccount extends AccountProcessors {

        @Override
        public String getCommand() {
            return "import";
        }

        @Override
        public String getCommandDescription() {
            return "balance <prikey> <password> --import an account by prikey";
        }

        @Override
        public boolean argsValidate(String[] args) {
            if (args.length != 3) {
                return false;
            }
            AssertUtil.canNotEmpty(args[1]);
            AssertUtil.canNotEmpty(args[2]);
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.importAccount(args[1],args[2]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }
}
