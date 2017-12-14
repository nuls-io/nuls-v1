package io.nuls.consensus.module.impl;

import io.nuls.account.entity.Account;
import io.nuls.account.service.intf.AccountService;
import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.ConsensusAccount;
import io.nuls.consensus.entity.genesis.DevGenesisBlock;
import io.nuls.consensus.entity.genesis.MainGenesisBlock;
import io.nuls.consensus.entity.genesis.TestGenesisBlock;
import io.nuls.consensus.entity.member.ConsensusAccountData;
import io.nuls.consensus.entity.tx.RedPunishTransaction;
import io.nuls.consensus.entity.tx.RegisterAgentTransaction;
import io.nuls.consensus.entity.tx.YellowPunishTransaction;
import io.nuls.consensus.entity.validator.block.PocBlockValidatorManager;
import io.nuls.consensus.event.*;
import io.nuls.consensus.handler.*;
import io.nuls.consensus.handler.filter.*;
import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.service.cache.ConsensusCacheService;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class PocConsensusModuleImpl extends AbstractConsensusModule {

    private NetworkProcessorService processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);
    private boolean delegatePeer = false;
    private ConsensusCacheService consensusCacheService = ConsensusCacheService.getInstance();
    private AccountService accountService = NulsContext.getInstance().getService(AccountService.class);

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
        delegatePeer = ConfigLoader.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_DELEGATE_PEER, false);
        PocBlockValidatorManager.initBlockValidators();
        this.registerService(BlockServiceImpl.getInstance());
        this.registerService(PocConsensusServiceImpl.getInstance());
        this.checkGenesisBlock();
        this.startBlockMaintenanceThread();
        this.checkConsensusStatus();
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_RED_PUNISH, RedPunishConsensusEvent.class);
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_YELLOW_PUNISH, YellowPunishConsensusEvent.class);
        this.registerEvent(PocConsensusConstant.EVENT_TYPE_REGISTER_AGENT, RegisterAgentEvent.class);
        this.registerTransaction(PocConsensusConstant.TX_TYPE_REGISTER_AGENT, RegisterAgentTransaction.class);
        this.registerTransaction(PocConsensusConstant.TX_TYPE_RED_PUNISH, RedPunishTransaction.class);
        this.registerTransaction(PocConsensusConstant.TX_TYPE_YELLOW_PUNISH, YellowPunishTransaction.class);
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
    }

    private void checkConsensusStatus() {
        //todo
        do {
            Account localAccount = accountService.getLocalAccount();
            if (null == localAccount) {
                Log.warn("local account is null!");
                break;
            }
            ConsensusAccount<ConsensusAccountData> memberSelf = consensusCacheService.getConsensusAccount(localAccount.getAddress().toString());
            if (null == memberSelf) {
                break;
            }
            if (memberSelf.getExtend().getStatus() == ConsensusStatusEnum.NOT_IN.getCode()) {
                break;
            }

        } while (false);


//        Judge own state
    }


    private void startBlockMaintenanceThread() {
        BlockMaintenanceThread blockMaintenanceThread = BlockMaintenanceThread.getInstance();
        blockMaintenanceThread.syncBlock();
        ThreadManager.createSingleThreadAndRun(this.getModuleId(),
                BlockMaintenanceThread.THREAD_NAME, blockMaintenanceThread);
    }

    /**
     * check genesis block
     * if genesis block isn't exist,create and download
     * exist: Verify it .
     */
    private void checkGenesisBlock() {
        //todo
    }

    @Override
    public void shutdown() {
        //todo
        ThreadManager.shutdownByModuleId(this.getModuleId());

    }

    @Override
    public void destroy() {
        //todo
    }

    @Override
    public String getInfo() {
        //todo 加入共识时间、出块数量、收益金额。。。
        return null;
    }

    @Override
    public int getVersion() {
        return PocConsensusConstant.POC_CONSENSUS_MODULE_VERSION;
    }

    public boolean isDelegatePeer() {
        return delegatePeer;
    }
}
