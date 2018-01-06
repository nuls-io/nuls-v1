package io.nuls.consensus.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.Consensus;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.genesis.GenesisBlock;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.tx.*;
import io.nuls.consensus.entity.validator.block.PocBlockValidatorManager;
import io.nuls.consensus.event.*;
import io.nuls.consensus.event.filter.*;
import io.nuls.consensus.event.handler.*;
import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.cache.manager.block.BlockHeaderCacheManager;
import io.nuls.consensus.cache.manager.member.ConsensusCacheManager;
import io.nuls.consensus.cache.manager.block.SmallBlockCacheManager;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.consensus.thread.BlockPersistenceThread;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.network.service.NetworkService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class PocConsensusModuleBootstrap extends AbstractConsensusModule {

    private EventBusService eventBusService = NulsContext.getInstance().getService(EventBusService.class);
    private boolean delegateNode = false;
    private ConsensusCacheManager consensusCacheManager;
    private AccountService accountService;

    @Override
    public void init() {
        consensusCacheManager = ConsensusCacheManager.getInstance();
        accountService = NulsContext.getInstance().getService(AccountService.class);
        NulsContext.getInstance().setGenesisBlock(GenesisBlock.getInstance());

        this.registerTransaction(TransactionConstant.TX_TYPE_REGISTER_AGENT, RegisterAgentTransaction.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_RED_PUNISH, RedPunishTransaction.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_YELLOW_PUNISH, YellowPunishTransaction.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_JOIN_CONSENSUS, PocJoinConsensusTransaction.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_EXIT_CONSENSUS, PocExitConsensusTransaction.class);
        delegateNode = NulsContext.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_DELEGATE_NODE, false);
        PocBlockValidatorManager.initBlockValidators();
        BlockCacheManager.getInstance().init();
        ConsensusCacheManager.getInstance().initCache();
        BlockHeaderCacheManager.getInstance().init();
        SmallBlockCacheManager.getInstance().init();

        //todo 接收处理 账户切换的notice，或者确认共识打包中不能切换账户
    }

    @Override
    public void start() {
        this.registerService(BlockServiceImpl.getInstance());
        this.registerService(PocConsensusServiceImpl.getInstance());
        this.startBlockMaintenanceThread();
        this.checkConsensusStatus();
        this.checkNodeType();
        TaskManager.createSingleThreadAndRun(this.getModuleId(), BlockPersistenceThread.THREAD_NAME, BlockPersistenceThread.getInstance());
        this.registerHandlers();
        Log.info("the POC consensus module is started!");

    }


    private void registerHandlers() {
        BlockEventHandler blockEventHandler = new BlockEventHandler();
        blockEventHandler.addFilter(new BlockEventFilter());
        eventBusService.subscribeNetworkEvent(BlockEvent.class, blockEventHandler);

        BlockHeaderHandler blockHeaderHandler = new BlockHeaderHandler();
        blockHeaderHandler.addFilter(new BlockHeaderEventFilter());
        eventBusService.subscribeNetworkEvent(BlockHeaderEvent.class, blockHeaderHandler);

        GetBlockHandler getBlockHandler = new GetBlockHandler();
        getBlockHandler.addFilter(new GetBlockEventFilter());
        eventBusService.subscribeNetworkEvent(GetSmallBlockEvent.class, getBlockHandler);

        GetTxGroupHandler getSmallBlockHandler = new GetTxGroupHandler();
        getSmallBlockHandler.addFilter(new GetTxGroupFilter());
        eventBusService.subscribeNetworkEvent(GetSmallBlockEvent.class, getSmallBlockHandler);

        RegisterAgentHandler registerAgentHandler = new RegisterAgentHandler();
        registerAgentHandler.addFilter(new RegisterAgentEventFilter());
        eventBusService.subscribeNetworkEvent(RegisterAgentEvent.class, registerAgentHandler);

        JoinConsensusHandler joinConsensusHandler = new JoinConsensusHandler();
        joinConsensusHandler.addFilter(AllreadyJoinConsensusEventFilter.getInstance());
        joinConsensusHandler.addFilter(CreditThresholdEventFilter.getInstance());
        eventBusService.subscribeNetworkEvent(JoinConsensusEvent.class, joinConsensusHandler);

        ExitConsensusHandler exitConsensusHandler = new ExitConsensusHandler();
        exitConsensusHandler.addFilter(new ExitConsensusEventFilter());
        eventBusService.subscribeNetworkEvent(ExitConsensusEvent.class, exitConsensusHandler);

        RedPunishHandler redPunishHandler = new RedPunishHandler();
        redPunishHandler.addFilter(new RedPunishEventFilter());
        eventBusService.subscribeNetworkEvent(RedPunishConsensusEvent.class, redPunishHandler);

        YellowPunishHandler yellowPunishHandler = new YellowPunishHandler();
        yellowPunishHandler.addFilter(new YellowPunishEventFilter());
        eventBusService.subscribeNetworkEvent(YellowPunishConsensusEvent.class, yellowPunishHandler);

        GetBlockHeaderHandler getBlockHeaderHandler = new GetBlockHeaderHandler();
        eventBusService.subscribeNetworkEvent(GetBlockHeaderEvent.class, getBlockHeaderHandler);

        TxGroupHandler txGroupHandler = new TxGroupHandler();
//todo        smallBlockHandler.addFilter();
        eventBusService.subscribeNetworkEvent(TxGroupEvent.class, txGroupHandler);
    }

    private void checkConsensusStatus() {
        if (!isDelegateNode()) {
            return;
        }
        Account localAccount = accountService.getLocalAccount();
        if (null == localAccount) {
            Log.warn("local account is null!");
            return;
        }
        Consensus<Agent> memberSelf =
                consensusCacheManager.getCachedAgent(localAccount.getAddress().toString());
        if (null == memberSelf) {
            return;
        }
        if (memberSelf.getExtend().getStatus() != ConsensusStatusEnum.NOT_IN.getCode()) {
            return;
        }
        startMining();
    }

    private void checkNodeType() {
        boolean isSeed = NulsContext.getInstance().getService(NetworkService.class).isSeed(null);
        if (!isSeed) {
            return;
        }
        Account localAccount = accountService.getLocalAccount();
        if (null == localAccount) {
            Log.warn("local account is null!");
            return;
        }
        this.startMining();
//        Consensus<Agent> memberSelf =
//                consensusCacheService.getConsensusAccount(localAccount.getAddress().toString());
//        if (null != memberSelf && memberSelf.getExtend().getStatus() != ConsensusStatusEnum.IN.getCode()) {
//            startMining();
//            return;
//        }
//        Map<String, Object> paramsMap = new HashMap<>();
//        paramsMap.put(JoinConsensusParam.IS_SEED_NODE, true);
//        paramsMap.put(JoinConsensusParam.AGENT_ADDRESS, localAccount.getAddress().toString());
//        paramsMap.put(JoinConsensusParam.DEPOSIT, 0L);
//        paramsMap.put(JoinConsensusParam.INTRODUCTION, "seed node!");
//        this.pocConsensusService.joinTheConsensus(localAccount.getAddress().toString(), null, paramsMap);
    }

    private void startMining() {
        TaskManager.createSingleThreadAndRun(this.getModuleId(),
                ConsensusMeetingRunner.THREAD_NAME,
                ConsensusMeetingRunner.getInstance());
    }


    private void startBlockMaintenanceThread() {
        BlockMaintenanceThread blockMaintenanceThread = BlockMaintenanceThread.getInstance();
        try {
            blockMaintenanceThread.checkGenesisBlock();
            blockMaintenanceThread.syncBlock();
        } catch (Exception e) {
            Log.error(e.getMessage());
        } finally {
            TaskManager.createSingleThreadAndRun(this.getModuleId(),
                    BlockMaintenanceThread.THREAD_NAME, blockMaintenanceThread);
        }
    }


    @Override
    public void shutdown() {
        TaskManager.shutdownByModuleId(this.getModuleId());
    }

    @Override
    public void destroy() {
        ConsensusCacheManager.getInstance().clear();
        BlockCacheManager.getInstance().clear();
        BlockHeaderCacheManager.getInstance().clear();
        SmallBlockCacheManager.getInstance().clear();
    }

    @Override
    public String getInfo() {
        if (this.getStatus() == ModuleStatusEnum.UNINITED || this.getStatus() == ModuleStatusEnum.INITING) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        str.append("module:[consensus]:\n");
        str.append("consensus status:");
        ConsensusStatusInfo statusInfo = this.consensusCacheManager.getConsensusStatusInfo(this.accountService.getDefaultAccount());
        if (null == statusInfo) {
            str.append(ConsensusStatusEnum.NOT_IN.getText());
        } else {
            str.append(ConsensusStatusEnum.getConsensusStatusByCode(statusInfo.getStatus()).getText());
        }
        str.append("\n");
        str.append("thread count:");
        List<BaseThread> threadList = TaskManager.getThreadList(this.getModuleId());
        if (null == threadList) {
            str.append(0);
        } else {
            str.append(threadList.size());
            for (BaseThread thread : threadList) {
                str.append("\n");
                str.append(thread.getName());
                str.append("{");
                str.append(thread.getPoolName());
                str.append("}");
            }
        }
        return str.toString();
    }

    @Override
    public int getVersion() {
        return PocConsensusConstant.POC_CONSENSUS_MODULE_VERSION;
    }

    public boolean isDelegateNode() {
        return delegateNode;
    }
}
