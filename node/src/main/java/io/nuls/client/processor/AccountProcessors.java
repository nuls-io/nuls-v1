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
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            //TODO 命令描述
            return "createaccount <password> <count> --create <count> accounts & Encrypting with with <password>";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
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
            return "getbalance";
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            return "getbalance <address> --get the balance of a address";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
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

    public static class GetWalletBalance extends AccountProcessors {

        @Override
        public String getCommand() {
            return "getwalletbalance";
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            return "getwalletbalance --get total balance of all addresses in the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getTotalBalance();
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class ListAccount extends AccountProcessors {

        @Override
        public String getCommand() {
            return "listaccount";
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            //TODO 命令描述
            return "listaccount --get total balance of all addresses in the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getAccountList();
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class GetUnspentUTXO extends AccountProcessors {

        @Override
        public String getCommand() {
            return "getunspentutxo";
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            //TODO 命令描述
            return "getunspentutxo --get total balance of all addresses in the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            //TODO 金额问题，SDK需要转成小数，RPC需要转成整数
            RpcClientResult result = accountService.getUnspentUTXO(args[1], Long.valueOf(args[2]));
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class AliasAccount extends AccountProcessors {

        @Override
        public String getCommand() {
            return "aliasaccount";
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            //TODO 命令描述
            return "aliasaccount --get total balance of all addresses in the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.setAlias(args[1], args[2], args[3]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }

    public static class GetPrivateKey extends AccountProcessors {

        @Override
        public String getCommand() {
            return "getprivatekey";
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            //TODO 命令描述
            return "getprivatekey --get total balance of all addresses in the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getPrivateKey(args[1], args[2]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }


    public static class GetAsset extends AccountProcessors {

        @Override
        public String getCommand() {
            return "getasset";
        }

        @Override
        public String getHelp() {
            return null;
        }

        @Override
        public String getCommandDescription() {
            //TODO 命令描述
            return "getasset --get total balance of all addresses in the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            //TODO 参数校验
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getAsset(args[1]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }



}
