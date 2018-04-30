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
import io.nuls.client.processor.*;
import io.nuls.client.processor.intf.CommandProcessor;
import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.rpc.constant.RpcConstant;
import io.nuls.rpc.sdk.SdkManager;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * @author Niels
 * @date 2017/10/26
 */
public class CommandHandler {

    public static final Map<String, CommandProcessor> PROCESSOR_MAP = new TreeMap<>();

    private void init() {
        register(new SystemProcessors.Exit());
        register(new SystemProcessors.Help());
        register(new SystemProcessors.Version());

        register(new BlockProcessors.GetBestBlockHeader());
        register(new BlockProcessors.GetBlock());
        register(new BlockProcessors.GetBlockHeader());
        register(new BlockProcessors.ListBlockHeader());

        register(new AccountProcessors.AliasAccount());
        register(new AccountProcessors.CreateAccount());
        register(new AccountProcessors.GetAccount());
        register(new AccountProcessors.GetAsset());
        register(new AccountProcessors.GetBalance());
        register(new AccountProcessors.GetPrivateKey());
        register(new AccountProcessors.GetUTXO());
        register(new AccountProcessors.GetWalletBalance());
        register(new AccountProcessors.ListAccount());

        register(new WalletProcessors.BackupWallet());
        register(new WalletProcessors.ImportAccount());
        register(new WalletProcessors.RemoveAccount());
        register(new WalletProcessors.ResetPassword());
        register(new WalletProcessors.Transfer());

        register(new ConsensusProcessors.GetConsensus());
        register(new ConsensusProcessors.GetConsensusAddress());
        register(new ConsensusProcessors.Agent());
        register(new ConsensusProcessors.Deposit());
        register(new ConsensusProcessors.GetAgent());
        register(new ConsensusProcessors.GetAgentStatus());
        register(new ConsensusProcessors.StopAgent());
        register(new ConsensusProcessors.Withdraw());
        register(new ConsensusProcessors.GetAllAgents());

        register(new TransactionProcessors.GetTx());
        register(new TransactionProcessors.GetTxList());

        register(new NetwrokProcessor.GetNetworkInfo());
        register(new NetwrokProcessor.getnetworknodes());

        sdkInit();
    }

    private void sdkInit() {
        String port = null;
        try {
            NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConstant.MODULES_CONFIG_FILE);
            port = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_PORT);
        }catch (IOException e) {
            Log.error("CommandHandler start failed", e);
            throw new NulsRuntimeException(ErrorCode.FAILED, "CommandHandler start failed");
        }catch (NulsException e) {
            // skip
        }
        if (StringUtils.isBlank(port)) {
            SdkManager.init("http://"+ RpcConstant.DEFAULT_IP + ":" + RpcConstant.DEFAULT_PORT);
        } else {
            /** release*/
            //SdkManager.init("http://"+ RpcConstant.DEFAULT_IP + ":" + port);
            /** test*/
            String ip = null;
            try {
                ip = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, "test.server.ip");
            } catch (NulsException e) {
                ip = RpcConstant.DEFAULT_IP;
            }
            SdkManager.init("http://" + ip + ":" + port);
        }

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
            System.out.print(instance.processCommand(read.split("\\s+")) + "\n" + CommandConstant.COMMAND_PS1);
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
        if(length == 2 && CommandConstant.NEED_HELP.equals(args[1])) {
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
