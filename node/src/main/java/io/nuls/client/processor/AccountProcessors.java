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

import io.nuls.rpc.sdk.entity.BalanceNa2NulsDto;
import io.nuls.client.entity.CommandResult;
import io.nuls.client.helper.CommandBulider;
import io.nuls.client.helper.CommandHelper;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.sdk.entity.BalanceDto;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.AccountService;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/3/27
 */
public abstract class AccountProcessors implements CommandProcessor {

    protected AccountService accountService = AccountService.ACCOUNT_SERVICE;

    /**
     * create accounts process
     */
    public static class CreateAccount extends AccountProcessors {

        @Override
        public String getCommand() {
            return "createaccount";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();

            builder.newLine(getCommandDescription())
                    .newLine("\t<password> the password your wallet, Required")
                    .newLine("\t<count> the count of account you want to create, Required");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "createaccount <password> <count> --create <count> accounts , encrypted by <password>";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
            // validate <count>
            if(!StringUtils.isNumeric(args[2])) {
                return false;
            }
            String pwd = args[1];
            CommandHelper.checkAndConfirmPwd(pwd);
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



    public static class GetAccount extends AccountProcessors {

        @Override
        public String getCommand() {
            return "getaccount";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> the account address - Required");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getaccount <address> --get account information";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getBaseInfo(args[1]);
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
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> the account address - require");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getbalance <address> --get the balance of a address";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getBalanceNa2Nuls(args[1]);
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
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription());
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getwalletbalance --get total balance of all addresses in the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length > 1) {
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getTotalBalanceNa2Nuls();
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
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription());
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "listaccount --get all addresses in the wallet";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length > 1) {
                return false;
            }
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

    public static class GetUTXO extends AccountProcessors {

        @Override
        public String getCommand() {
            return "getutxo";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> the account address - Required")
                    .newLine("\t<amount> account, utxo - Required");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getutxo <address> <amount> --get a minimal list of UTXO,the total amount of which is greater than <amount>.";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
            Long amount = CommandHelper.getLongAmount(args[2]);
            if(amount == null) {
                return false;
            }
            args[2] = amount.toString();
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getUTXONa2Nuls(args[1], Long.valueOf(args[2]));
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
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t<alias> alias - Required")
                    .newLine("\t<address> address - Required")
                    .newLine("\t<password> password - Required");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "aliasaccount <alias> <address> <password>--create a nickname";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 4) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
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
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> address - Required")
                    .newLine("\t<password> password - Required");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getprivatekey <address> <password>--get the private key of your account";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
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
            CommandBulider builder = new CommandBulider();
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> address - Required");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getasset <address>--get your assets";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2) {
                return false;
            }
            if(!CommandHelper.checkArgsIsNull(args)) {
                return false;
            }
            return true;
        }

        @Override
        public CommandResult execute(String[] args) {
            RpcClientResult result = accountService.getAssetNa2Nuls(args[1]);
            if (null == result) {
                return CommandResult.getFailed("Failure to execute");
            }
            return CommandResult.getResult(result);
        }
    }



}
