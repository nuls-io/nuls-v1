package io.nuls.consensus.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.genesis.DevGenesisBlock;
import io.nuls.consensus.entity.genesis.MainGenesisBlock;
import io.nuls.consensus.entity.genesis.TestGenesisBlock;
import io.nuls.consensus.entity.member.Agent;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.entity.validator.block.PocBlockValidatorManager;
import io.nuls.consensus.event.*;
import io.nuls.consensus.handler.*;
import io.nuls.consensus.handler.filter.*;
import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.ConsensusCacheService;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.service.intf.ConsensusService;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.consensus.thread.ConsensusMeetingThread;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.network.service.NetworkService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class PocConsensusModuleImpl extends AbstractConsensusModule {

    private NetworkProcessorService processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);
    private boolean delegatePeer = false;
    private ConsensusCacheService consensusCacheService = ConsensusCacheService.getInstance();
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);
    private ConsensusService pocConsensusService = PocConsensusServiceImpl.getInstance();

    @Override
    public void start() {
        try {
            String mode = ConfigLoader.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_RUN_MODE);
            if (NulsConstant.RUN_MODE_MAIN.equals(mode)) {
                NulsContext.getInstance().setGenesisBlock(MainGenesisBlock.getInstance());
            } else if (NulsConstant.RUN_MODE_TEST.equals(mode)) {
                NulsContext.getInstance().setGenesisBlock(TestGenesisBlock.getInstance());
            } else {
                NulsContext.getInstance().setGenesisBlock(DevGenesisBlock.getInstance());
            }
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_RED_PUNISH, RedPunishConsensusEvent.class);
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_YELLOW_PUNISH, YellowPunishConsensusEvent.class);
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_REGISTER_AGENT, RegisterAgentEvent.class);
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_ASK_BLOCK, AskBlockInfoEvent.class);
        this.registerTransaction(PocConsensusConstant.TX_TYPE_REGISTER_AGENT, RegisterAgentTransaction.class);
        this.registerTransaction(PocConsensusConstant.TX_TYPE_RED_PUNISH, RedPunishTransaction.class);
        this.registerTransaction(PocConsensusConstant.TX_TYPE_YELLOW_PUNISH, YellowPunishTransaction.class);
        delegatePeer = ConfigLoader.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_DELEGATE_PEER, false);
        PocBlockValidatorManager.initBlockValidators();
        this.registerService(BlockServiceImpl.getInstance());
        this.registerService(PocConsensusServiceImpl.getInstance());
        this.startBlockMaintenanceThread();
        this.checkConsensusStatus();
        this.checkPeerType();

        this.registerHanders();
        Log.info("the POC consensus module is started!");

    }


    private void registerHanders() {
        BlockEventHandler blockEventHandler = new BlockEventHandler();
        blockEventHandler.addFilter(new BlockEventFilter());
        processorService.registerEventHandler(BlockEvent.class, blockEventHandler);

        BlockHeaderHandler blockHeaderHandler = new BlockHeaderHandler();
        blockHeaderHandler.addFilter(new BlockHeaderEventFilter());
        processorService.registerEventHandler(BlockHeaderEvent.class, blockHeaderHandler);

        GetBlockHandler getBlockHandler = new GetBlockHandler();
        getBlockHandler.addFilter(new GetBlockEventFilter());
        processorService.registerEventHandler(GetBlockEvent.class, getBlockHandler);

        RegisterAgentHandler registerAgentHandler = new RegisterAgentHandler();
        registerAgentHandler.addFilter(new RegisterAgentEventFilter());
        processorService.registerEventHandler(RegisterAgentEvent.class, registerAgentHandler);

        JoinConsensusHandler joinConsensusHandler = new JoinConsensusHandler();
        joinConsensusHandler.addFilter(new JoinConsensusEventFilter());
        processorService.registerEventHandler(JoinConsensusEvent.class, joinConsensusHandler);

        ExitConsensusHandler exitConsensusHandler = new ExitConsensusHandler();
        exitConsensusHandler.addFilter(new ExitConsensusEventFilter());
        processorService.registerEventHandler(ExitConsensusEvent.class, exitConsensusHandler);

        RedPunishHandler redPunishHandler = new RedPunishHandler();
        redPunishHandler.addFilter(new RedPunishEventFilter());
        processorService.registerEventHandler(RedPunishConsensusEvent.class, redPunishHandler);

        YellowPunishHandler yellowPunishHandler = new YellowPunishHandler();
        yellowPunishHandler.addFilter(new YellowPunishEventFilter());
        processorService.registerEventHandler(YellowPunishConsensusEvent.class, yellowPunishHandler);

        AskBlockInfoHandler askBlockInfoHandler = new AskBlockInfoHandler();
        processorService.registerEventHandler(AskBlockInfoEvent.class, askBlockInfoHandler);

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
        ConsensusAccount<Agent> memberSelf =
                consensusCacheService.getConsensusAccount(localAccount.getAddress().toString());
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
        int i = consensusCacheService.getDelegateAccountCount();
        if (i <= PocConsensusConstant.SAFELY_CONSENSUS_COUNT) {
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("isSeed", "true");
            this.pocConsensusService.joinTheConsensus(localAccount.getAddress().toString(), null, paramsMap);
        }
    }

    private void startMining() {
        ThreadManager.createSingleThreadAndRun(this.getModuleId(),
                ConsensusMeetingThread.THREAD_NAME,
                ConsensusMeetingThread.getInstance());
    }


    private void startBlockMaintenanceThread() {
        BlockMaintenanceThread blockMaintenanceThread = BlockMaintenanceThread.getInstance();
        try {
            blockMaintenanceThread.checkGenesisBlock();
            blockMaintenanceThread.syncBlock();
        } catch (Exception e) {
            Log.error(e);
        } finally {
            ThreadManager.createSingleThreadAndRun(this.getModuleId(),
                    BlockMaintenanceThread.THREAD_NAME, blockMaintenanceThread);
        }
    }


    @Override
    public void shutdown() {
        ConsensusCacheService.getInstance().clear();
        BlockCacheService.getInstance().clear();
        ThreadManager.shutdownByModuleId(this.getModuleId());
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getInfo() {
        StringBuilder str = new StringBuilder();
        str.append("module:[consensus]:\n");
        str.append("consensus status:");
        ConsensusStatusInfo statusInfo = this.consensusCacheService.getConsensusStatusInfo();
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
