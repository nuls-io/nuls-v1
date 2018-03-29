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
     * create accounts processor
     */
    public static class CreateAccount extends AccountProcessors {

        @Override
        public String getCommand() {
            return "createaccount";
        }

        @Override
        public String getHelp() {
            CommandBulider builder = new CommandBulider();
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<password> 钱包密码, 若没有则新建 - 必输")
                    .newLine("\t<count> 创建账户数量 - 必输");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "createaccount <password> <count> --create <count> accounts & Encrypting with <password>";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
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
                    .newLine("\t<address> the account address - require");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            //TODO 翻译
            return "getaccount <address> --获取账户基本信息";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
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
            if(length != 2)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
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
            if(length > 1)
                return false;
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
            if(length > 1)
                return false;
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
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> the account address - require")
                    .newLine("\t<amount> 金额，本命令会返回等于或大于此金额的utxo - require");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            return "getutxo <address> <amount> --Obtain sufficient amount of utxo from the account.";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
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
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<alias> 别名 - 必输")
                    .newLine("\t<address> 账户地址 - 必输")
                    .newLine("\t<password> 钱包密码 - 必输");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            //TODO 翻译
            return "aliasaccount <alias> <address> <password>--为账户设置别名";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 4)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
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
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> 账户地址 - 必输")
                    .newLine("\t<password> 钱包密码 - 必输");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            //TODO 翻译
            return "getprivatekey <address> <password>--查询账户私钥，只能查询本地创建或导入的账户";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 3)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
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
            //TODO 翻译
            builder.newLine(getCommandDescription())
                    .newLine("\t<address> 账户地址 - 必输");
            return builder.toString();
        }

        @Override
        public String getCommandDescription() {
            //TODO 翻译
            return "getasset <address>--查询账户资产";
        }

        @Override
        public boolean argsValidate(String[] args) {
            int length = args.length;
            if(length != 2)
                return false;
            if(!CommandHelper.checkArgsIsNull(args))
                return false;
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
