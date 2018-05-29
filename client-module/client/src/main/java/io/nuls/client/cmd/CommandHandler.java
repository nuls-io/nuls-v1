package io.nuls.client.cmd;

import io.nuls.account.rpc.cmd.*;
import io.nuls.accout.ledger.rpc.cmd.GetAccountTxListProcessor;
import io.nuls.accout.ledger.rpc.cmd.GetUTXOProcessor;
import io.nuls.accout.ledger.rpc.cmd.TransferProcessor;
import io.nuls.client.constant.CommandConstant;
import io.nuls.client.rpc.constant.RpcConstant;
import io.nuls.consensus.poc.rpc.cmd.*;
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
import io.nuls.protocol.rpc.cmd.GetBestBlockHeaderProcessor;
import io.nuls.protocol.rpc.cmd.GetBlockHeaderProcessor;
import io.nuls.protocol.rpc.cmd.GetBlockProcessor;

import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class CommandHandler {

    public static final Map<String, CommandProcessor> PROCESSOR_MAP = new TreeMap<>();

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
        register(new CreateAccountProcessor());
        register(new CreateAccountsProcessor());
        register(new GetAccountProcessor());
        register(new GetAccountsProcessor());
        register(new GetAssetProcessor());
        register(new GetBalanceProcessor());
        register(new GetWalletBalanceProcessor());
        register(new GetPrivateKeyProcessor());
        register(new ImportByKeyStoreProcessor());
        register(new ImportByPrivateKeyProcessor());
        register(new RemoveAccountProcessor());
        register(new ResetPasswordProcessor());
        register(new SetAliasProcessor());
        register(new SetPasswordProcessor());

        /**
         * accountLedger
         */
        register(new TransferProcessor());
        register(new GetAccountTxListProcessor());
        register(new GetUTXOProcessor());

        /**
         * consensus
         */
        register(new CreateAgentProcessor());
        register(new GetConsensusProcessor());
        register(new GetConsensusAddressProcessor());
        register(new DepositProcessor());
        register(new WithdrawProcessor());
        register(new StopAgentProcessor());
        register(new GetAgentProcessor());
        register(new GetAgentsProcessor());
        register(new GetDepositedAgentsProcessor());
        register(new GetDepositedsProcessor());
        register(new GetDepositedInfoProcessor());


        sdkInit();
    }


    private void sdkInit() {
        String port = null;
        try {
            NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConstant.MODULES_CONFIG_FILE);
            port = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_PORT);
        } catch (Exception e) {
            Log.error("CommandHandler start failed", e);
            throw new NulsRuntimeException(KernelErrorCode.FAILED, "CommandHandler start failed");
        }
        if (StringUtils.isBlank(port)) {
            RestFulUtils.getInstance().setServerUri("http://" + RpcConstant.DEFAULT_IP + ":" + RpcConstant.DEFAULT_PORT);
        } else {
            String ip = null;
            try {
                ip = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, "test.server.ip");
            } catch (Exception e) {
                ip = RpcConstant.DEFAULT_IP;
            }
            RestFulUtils.getInstance().setServerUri("http://" + ip + ":" + port);
        }
    }


    public static void main(String[] args) {
        CommandHandler instance = new CommandHandler();
        instance.init();
        try {
            I18nUtils.setLanguage("en");
        } catch (NulsException e) {
            e.printStackTrace();
        }
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
