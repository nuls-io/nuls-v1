package io.nuls.consensus.module.impl;

import io.nuls.consensus.constant.POCConsensusConstant;
import io.nuls.consensus.entity.RedPunishTransaction;
import io.nuls.consensus.entity.RegisterAgentTransaction;
import io.nuls.consensus.entity.YellowPunishTransaction;
import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.consensus.event.RedPunishConsensusEvent;
import io.nuls.consensus.event.YellowPunishConsensusEvent;
import io.nuls.consensus.handler.JoinConsensusHandler;
import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.service.impl.POCConsensusServiceImpl;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class POCConsensusModuleImpl extends AbstractConsensusModule {

    private NetworkProcessorService processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);
    private boolean delegatePeer = false;

    @Override
    public void start() {
        delegatePeer = ConfigLoader.getCfgValue(POCConsensusConstant.CFG_CONSENSUS_SECTION, POCConsensusConstant.PROPERTY_DELEGATE_PEER, false);
        this.checkGenesisBlock();
        this.checkBlockHeight();
        this.checkConsensusStatus();
        this.startBlockMaintenanceThread();
        this.registerHanders();
        this.registerService(POCConsensusServiceImpl.getInstance());
        this.registerEvent(POCConsensusConstant.EVENT_TYPE_RED_PUNISH, RedPunishConsensusEvent.class);
        this.registerEvent(POCConsensusConstant.EVENT_TYPE_YELLOW_PUNISH, YellowPunishConsensusEvent.class);
        this.registerTransaction(POCConsensusConstant.TX_TYPE_REGISTER_AGENT, RegisterAgentTransaction.class);
        this.registerTransaction(POCConsensusConstant.TX_TYPE_RED_PUNISH, RedPunishTransaction.class);
        this.registerTransaction(POCConsensusConstant.TX_TYPE_YELLOW_PUNISH, YellowPunishTransaction.class);
        Log.info("the POC consensus module is started!");

    }

    private void registerHanders() {
        //todo
        JoinConsensusHandler joinConsensusHandler = new JoinConsensusHandler();
        //add filter , add validator
        processorService.registerEventHandler(JoinConsensusEvent.class, joinConsensusHandler);
    }

    private void checkConsensusStatus() {
        //todo
    }

    private void checkBlockHeight() {
        //todo
    }

    private void startBlockMaintenanceThread() {
        ThreadManager.createSingleThreadAndRun(this.getModuleId(),
                "BlockMaintenance", BlockMaintenanceThread.getInstance());
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
        return POCConsensusConstant.POC_CONSENSUS_MODULE_VERSION;
    }

    public boolean isDelegatePeer() {
        return delegatePeer;
    }
}
