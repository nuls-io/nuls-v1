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
import io.nuls.consensus.handler.*;
import io.nuls.consensus.handler.filter.*;
import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.BlockHeaderCacheService;
import io.nuls.consensus.service.cache.ConsensusCacheService;
import io.nuls.consensus.service.cache.SmallBlockCacheService;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.consensus.thread.BlockPersistenceThread;
import io.nuls.consensus.thread.ConsensusMeetingRunner;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventConsumer;
import io.nuls.network.service.NetworkService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class PocConsensusModuleBootstrap extends AbstractConsensusModule {

    private EventConsumer eventConsumer = NulsContext.getInstance().getService(EventConsumer.class);
    private boolean delegatePeer = false;
    private ConsensusCacheService consensusCacheService;
    private AccountService accountService;

    @Override
    public void init() {
        consensusCacheService = ConsensusCacheService.getInstance();
        accountService = NulsContext.getInstance().getService(AccountService.class);
        NulsContext.getInstance().setGenesisBlock(GenesisBlock.getInstance());

        this.registerTransaction(TransactionConstant.TX_TYPE_REGISTER_AGENT, RegisterAgentTransaction.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_RED_PUNISH, RedPunishTransaction.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_YELLOW_PUNISH, YellowPunishTransaction.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_JOIN_CONSENSUS, PocJoinConsensusTransaction.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_EXIT_CONSENSUS, PocExitConsensusTransaction.class);
        delegatePeer = ConfigLoader.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_DELEGATE_PEER, false);
        PocBlockValidatorManager.initBlockValidators();
        BlockCacheService.getInstance().init();
        ConsensusCacheService.getInstance().initCache();
        BlockHeaderCacheService.getInstance().init();
        SmallBlockCacheService.getInstance().init();

        //todo 接收处理 账户切换的notice，或者确认共识打包中不能切换账户
    }

    @Override
    public void start() {
        this.registerService(BlockServiceImpl.getInstance());
        this.registerService(PocConsensusServiceImpl.getInstance());
        this.startBlockMaintenanceThread();
        this.checkConsensusStatus();
        this.checkPeerType();
        ThreadManager.createSingleThreadAndRun(this.getModuleId(), BlockPersistenceThread.THREAD_NAME, BlockPersistenceThread.getInstance());
        this.registerHanders();
        Log.info("the POC consensus module is started!");

    }


    private void registerHanders() {
        BlockEventHandler blockEventHandler = new BlockEventHandler();
        blockEventHandler.addFilter(new BlockEventFilter());
        eventConsumer.subscribeNetworkEvent(BlockEvent.class, blockEventHandler);

        BlockHeaderHandler blockHeaderHandler = new BlockHeaderHandler();
        blockHeaderHandler.addFilter(new BlockHeaderEventFilter());
        eventConsumer.subscribeNetworkEvent(BlockHeaderEvent.class, blockHeaderHandler);

        GetBlockHandler getBlockHandler = new GetBlockHandler();
        getBlockHandler.addFilter(new GetBlockEventFilter());
        eventConsumer.subscribeNetworkEvent(GetSmallBlockEvent.class, getBlockHandler);

        GetTxGroupHandler getSmallBlockHandler = new GetTxGroupHandler();
        getSmallBlockHandler.addFilter(new GetTxGroupFilter());
        eventConsumer.subscribeNetworkEvent(GetSmallBlockEvent.class, getSmallBlockHandler);

        RegisterAgentHandler registerAgentHandler = new RegisterAgentHandler();
        registerAgentHandler.addFilter(new RegisterAgentEventFilter());
        eventConsumer.subscribeNetworkEvent(RegisterAgentEvent.class, registerAgentHandler);

        JoinConsensusHandler joinConsensusHandler = new JoinConsensusHandler();
        joinConsensusHandler.addFilter(AllreadyJoinConsensusEventFilter.getInstance());
        joinConsensusHandler.addFilter(CreditThresholdEventFilter.getInstance());
        eventConsumer.subscribeNetworkEvent(JoinConsensusEvent.class, joinConsensusHandler);

        ExitConsensusHandler exitConsensusHandler = new ExitConsensusHandler();
        exitConsensusHandler.addFilter(new ExitConsensusEventFilter());
        eventConsumer.subscribeNetworkEvent(ExitConsensusEvent.class, exitConsensusHandler);

        RedPunishHandler redPunishHandler = new RedPunishHandler();
        redPunishHandler.addFilter(new RedPunishEventFilter());
        eventConsumer.subscribeNetworkEvent(RedPunishConsensusEvent.class, redPunishHandler);

        YellowPunishHandler yellowPunishHandler = new YellowPunishHandler();
        yellowPunishHandler.addFilter(new YellowPunishEventFilter());
        eventConsumer.subscribeNetworkEvent(YellowPunishConsensusEvent.class, yellowPunishHandler);

        GetBlockHeaderHandler getBlockHeaderHandler = new GetBlockHeaderHandler();
        eventConsumer.subscribeNetworkEvent(GetBlockHeaderEvent.class, getBlockHeaderHandler);

        TxGroupHandler txGroupHandler = new TxGroupHandler();
//todo        smallBlockHandler.addFilter();
        eventConsumer.subscribeNetworkEvent(TxGroupEvent.class, txGroupHandler);
    }

    private void checkConsensusStatus() {
        if (!isDelegatePeer()) {
            return;
        }
        Account localAccount = accountService.getLocalAccount();
        if (null == localAccount) {
            Log.warn("local account is null!");
            return;
        }
        Consensus<Agent> memberSelf =
                consensusCacheService.getCachedAgent(localAccount.getAddress().toString());
        if (null == memberSelf) {
            return;
        }
        if (memberSelf.getExtend().getStatus() != ConsensusStatusEnum.NOT_IN.getCode()) {
            return;
        }
        startMining();
    }

    private void checkPeerType() {
        boolean isSeed = NulsContext.getInstance().getService(NetworkService.class).isSeedPeer(null);
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
//        paramsMap.put(JoinConsensusParam.IS_SEED_PEER, true);
//        paramsMap.put(JoinConsensusParam.AGENT_ADDRESS, localAccount.getAddress().toString());
//        paramsMap.put(JoinConsensusParam.DEPOSIT, 0L);
//        paramsMap.put(JoinConsensusParam.INTRODUCTION, "seed peer!");
//        this.pocConsensusService.joinTheConsensus(localAccount.getAddress().toString(), null, paramsMap);
    }

    private void startMining() {
        ThreadManager.createSingleThreadAndRun(this.getModuleId(),
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
            ThreadManager.createSingleThreadAndRun(this.getModuleId(),
                    BlockMaintenanceThread.THREAD_NAME, blockMaintenanceThread);
        }
    }


    @Override
    public void shutdown() {
        ThreadManager.shutdownByModuleId(this.getModuleId());
    }

    @Override
    public void destroy() {
        ConsensusCacheService.getInstance().clear();
        BlockCacheService.getInstance().clear();
        BlockHeaderCacheService.getInstance().clear();
        SmallBlockCacheService.getInstance().clear();
    }

    @Override
    public String getInfo() {
        if (this.getStatus() == ModuleStatusEnum.UNINITED || this.getStatus() == ModuleStatusEnum.INITING) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        str.append("module:[consensus]:\n");
        str.append("consensus status:");
        ConsensusStatusInfo statusInfo = this.consensusCacheService.getConsensusStatusInfo(this.accountService.getDefaultAccount());
        if (null == statusInfo) {
            str.append(ConsensusStatusEnum.NOT_IN.getText());
        } else {
            str.append(ConsensusStatusEnum.getConsensusStatusByCode(statusInfo.getStatus()).getText());
        }
        str.append("\n");
        str.append("thread count:");
        List<BaseThread> threadList = ThreadManager.getThreadList(this.getModuleId());
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

    public boolean isDelegatePeer() {
        return delegatePeer;
    }
}
