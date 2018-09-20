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

import io.nuls.account.rpc.cmd.*;
import io.nuls.accout.ledger.rpc.cmd.*;
import io.nuls.client.constant.CommandConstant;
import io.nuls.client.rpc.constant.RpcConstant;
import io.nuls.consensus.poc.rpc.cmd.*;
import io.nuls.contract.rpc.cmd.GetContractBalanceProcessor;
import io.nuls.contract.rpc.cmd.GetContractInfoProcessor;
import io.nuls.contract.rpc.cmd.GetContractResultProcessor;
import io.nuls.contract.rpc.cmd.GetContractTxProcessor;
import io.nuls.core.tools.cfg.ConfigLoader;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.i18n.I18nUtils;
import io.nuls.kernel.processor.CommandProcessor;
import io.nuls.kernel.utils.RestFulUtils;
import io.nuls.ledger.rpc.cmd.GetTxProcessor;
import io.nuls.network.rpc.cmd.GetNetInfoProcessor;
import io.nuls.network.rpc.cmd.GetNetNodesProcessor;
import io.nuls.protocol.rpc.cmd.GetBestBlockHeaderProcessor;
import io.nuls.protocol.rpc.cmd.GetBlockHeaderProcessor;
import io.nuls.protocol.rpc.cmd.GetBlockProcessor;
import io.nuls.utxo.accounts.rpc.cmd.GetUtxoAccountsProcessor;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import java.io.IOException;
import java.util.*;

public class CommandHandler {

    public static final Map<String, CommandProcessor> PROCESSOR_MAP = new TreeMap<>();

    public static ConsoleReader CONSOLE_READER;

    public CommandHandler() {

    }

    /**
     * 初始化加载所有命令行实现
     */
    private void init() {
        /**
         * ledger
         */
        register(new GetTxProcessor());

        /**
         * block
         */
        register(new GetBlockHeaderProcessor());
        register(new GetBlockProcessor());
        register(new GetBestBlockHeaderProcessor());

        /**
         * account
         */
        register(new BackupAccountProcessor());
        register(new CreateProcessor());
        register(new GetAccountProcessor());
        register(new GetAccountsProcessor());
//        register(new GetAssetProcessor());//
        register(new GetBalanceProcessor());
//        register(new GetWalletBalanceProcessor());//
        register(new GetPrivateKeyProcessor());
        register(new ImportByKeyStoreProcessor());
        register(new ImportByPrivateKeyProcessor());
        register(new ImportForcedByPrivateKeyProcessor());
        register(new RemoveAccountProcessor());
        register(new ResetPasswordProcessor());
        register(new SetAliasProcessor());
        register(new SetPasswordProcessor());

        /**
         * Multi-signature account
         */
        register(new CreateMultiSigAccountProcessor());
        register(new ImportMultiSigAccountProcessor());
        register(new GetMultiSigAccountListProcessor());
        register(new GetMultiSigAccountProcessor());
        register(new RemoveMultiSigAccountProcessor());
        register(new GetMultiSigAccountCountProcessor());
        register(new CreateMultiSigAccountProcessor());

        register(new CreateMultiTransferProcess());
        register(new SignMultiTransactionProcess());
        register(new CreateMultiAliasProcess());
        register(new CreateMultiAgentProcessor());
        register(new CreateMultiDepositProcessor());
        register(new CreateMultiWithdrawProcessor());
        register(new CreateMultiStopAgentProcessor());

        /**
         * accountLedger
         */
        register(new TransferProcessor());
        register(new GetAccountTxListProcessor());
//        register(new GetUTXOProcessor());//

        /**
         * consensus
         */
        register(new CreateAgentProcessor());
        register(new GetConsensusProcessor());
        register(new DepositProcessor());
        register(new WithdrawProcessor());
        register(new StopAgentProcessor());
        register(new GetAgentProcessor());
        register(new GetAgentsProcessor());
        register(new GetDepositedAgentsProcessor());
        register(new GetDepositedsProcessor());
        register(new GetDepositedInfoProcessor());

        /**
         * network
         */
        register(new GetNetInfoProcessor());
        register(new GetNetNodesProcessor());

        /**
         * system
         */
        register(new ExitProcessor());
        register(new HelpProcessor());
        register(new VersionProcessor());
        register(new UpgradeProcessor());
        /**
         * utxoAccounts
         */
        register(new GetUtxoAccountsProcessor());

        /**
         * contract
         */
        register(new GetContractTxProcessor());
        register(new GetContractResultProcessor());
        register(new GetContractInfoProcessor());
        register(new GetContractBalanceProcessor());
        sdkInit();
    }

    private void sdkInit() {
        String port = null;
        try {
            NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConstant.MODULES_CONFIG_FILE);
            port = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_PORT);
        } catch (Exception e) {
            Log.error("CommandHandler start failed", e);
            throw new NulsRuntimeException(KernelErrorCode.FAILED);
        }
        if (StringUtils.isBlank(port)) {
            RestFulUtils.getInstance().setServerUri("http://" + RpcConstant.DEFAULT_IP + ":" + RpcConstant.DEFAULT_PORT + RpcConstant.PREFIX);
        } else {
            String ip = null;
            try {
                ip = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, "server.ip").trim();
                if ("0.0.0.0".equals(ip)) {
                    ip = RpcConstant.DEFAULT_IP;
                }
            } catch (Exception e) {
                ip = RpcConstant.DEFAULT_IP;
            }
            RestFulUtils.getInstance().setServerUri("http://" + ip + ":" + port + RpcConstant.PREFIX);
        }
    }

    public static void main(String[] args) {
        /**
         * 如果操作系统是windows, 可能会使控制台读取部分处于死循环，可以设置为false，绕过本地Windows API，直接使用Java IO流输出
         * If the operating system is windows, it may cause the console to read part of the loop, can be set to false,
         * bypass the native Windows API, use the Java IO stream output directly
         */
        if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
            System.setProperty("jline.WindowsTerminal.directConsole", "false");
        }
        CommandHandler instance = new CommandHandler();
        instance.init();
        try {
            I18nUtils.setLanguage("en");
        } catch (NulsException e) {
            e.printStackTrace();
        }
        try {
            CONSOLE_READER = new ConsoleReader();
            List<Completer> completers = new ArrayList<Completer>();
            completers.add(new StringsCompleter(PROCESSOR_MAP.keySet()));
            CONSOLE_READER.addCompleter(new ArgumentCompleter(completers));
            String line = null;
            do {
                line = CONSOLE_READER.readLine(CommandConstant.COMMAND_PS1);
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                System.out.print(instance.processCommand(line.split("\\s+")) + "\n");
            } while (line != null);
        } catch (IOException e) {

        } finally {
            try {
                if (!CONSOLE_READER.delete()) {
                    CONSOLE_READER.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String processCommand(String[] args) {
        int length = args.length;
        if (length == 0) {
            return CommandConstant.COMMAND_ERROR;
        }
        String command = args[0];
        CommandProcessor processor = PROCESSOR_MAP.get(command);
        if (processor == null) {
            return command + " not a nuls command!";
        }
        if (length == 2 && CommandConstant.NEED_HELP.equals(args[1])) {
            return processor.getHelp();
        }
        try {
            boolean result = processor.argsValidate(args);
            if (!result) {
                return "args incorrect:\n" + processor.getHelp();
            }
            return processor.execute(args).toString();
        } catch (Exception e) {
            return CommandConstant.EXCEPTION + ": " + e.getMessage();
        }
    }

    private void register(CommandProcessor processor) {
        PROCESSOR_MAP.put(processor.getCommand(), processor);
    }
}
