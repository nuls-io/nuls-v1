package io.nuls.consensus.module.impl;

import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.tx.*;
import io.nuls.consensus.entity.validator.PocBlockValidatorManager;
import io.nuls.consensus.event.*;
import io.nuls.consensus.event.filter.*;
import io.nuls.consensus.event.handler.*;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.service.tx.*;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.aop.AopUtils;
import io.nuls.core.utils.log.Log;
import io.nuls.db.transactional.TransactionalAopFilter;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.ledger.event.TransactionEvent;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class PocConsensusModuleBootstrap extends AbstractConsensusModule {

    private EventBusService eventBusService = NulsContext.getInstance().getService(EventBusService.class);

    private ConsensusManager consensusManager = ConsensusManager.getInstance();

    @Override
    public void init() {
        PocBlockValidatorManager.initBlockValidators();
        initTransactions();
        consensusManager.init();
    }

    private void initTransactions() {
        this.registerTransaction(TransactionConstant.TX_TYPE_REGISTER_AGENT, RegisterAgentTransaction.class, new RegisterAgentTxService());
        this.registerTransaction(TransactionConstant.TX_TYPE_RED_PUNISH, RedPunishTransaction.class, new RedPunishTxService());
        this.registerTransaction(TransactionConstant.TX_TYPE_YELLOW_PUNISH, YellowPunishTransaction.class, new YellowPunishTxService());
        this.registerTransaction(TransactionConstant.TX_TYPE_JOIN_CONSENSUS, PocJoinConsensusTransaction.class, new JoinConsensusTxService());
        this.registerTransaction(TransactionConstant.TX_TYPE_EXIT_CONSENSUS, PocExitConsensusTransaction.class, new ExitConsensusTxService());
    }

    @Override
    public void start() {
        this.registerService(BlockService.class, AopUtils.createProxy(BlockServiceImpl.class,
                NulsContext.getInstance().getService(TransactionalAopFilter.class)));
        this.registerService(PocConsensusServiceImpl.getInstance());

        this.consensusManager.startMaintenanceWork();
        ConsensusStatusInfo statusInfo = consensusManager.getConsensusStatusInfo();
        if (statusInfo.getStatus() != ConsensusStatusEnum.NOT_IN.getCode()) {
            consensusManager.joinMeeting();
        }
        consensusManager.startPersistenceWork();
        this.registerHandlers();
        Log.info("the POC consensus module is started!");

    }


    private void registerHandlers() {
        BlockEventHandler blockEventHandler = new BlockEventHandler();
        blockEventHandler.addFilter(new BlockEventFilter());
        eventBusService.subscribeEvent(BlockEvent.class, blockEventHandler);

        BlockHeaderHandler blockHeaderHandler = new BlockHeaderHandler();
        blockHeaderHandler.addFilter(new BlockHeaderEventFilter());
        eventBusService.subscribeEvent(BlockHeaderEvent.class, blockHeaderHandler);

        GetBlockHandler getBlockHandler = new GetBlockHandler();
        getBlockHandler.addFilter(new GetBlockEventFilter());
        eventBusService.subscribeEvent(GetSmallBlockEvent.class, getBlockHandler);

        GetTxGroupHandler getSmallBlockHandler = new GetTxGroupHandler();
        getSmallBlockHandler.addFilter(new GetTxGroupFilter());
        eventBusService.subscribeEvent(GetSmallBlockEvent.class, getSmallBlockHandler);

        GetBlockHeaderHandler getBlockHeaderHandler = new GetBlockHeaderHandler();
        eventBusService.subscribeEvent(GetBlockHeaderEvent.class, getBlockHeaderHandler);

        TxGroupHandler txGroupHandler = new TxGroupHandler();
        //todo        smallBlockHandler.addFilter();
        eventBusService.subscribeEvent(TxGroupEvent.class, txGroupHandler);

        NewTxEventHandler newTxEventHandler = NewTxEventHandler.getInstance();
        newTxEventHandler.addFilter(new NewTxEventFilter());
        eventBusService.subscribeEvent(TransactionEvent.class, newTxEventHandler);
    }


    @Override
    public void shutdown() {
        TaskManager.shutdownByModuleId(this.getModuleId());
    }

    @Override
    public void destroy() {
        consensusManager.destroy();
    }

    @Override
    public String getInfo() {
        if (this.getStatus() == ModuleStatusEnum.UNINITED || this.getStatus() == ModuleStatusEnum.INITING) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        str.append("module:[consensus]:\n");
        str.append("consensus status:");
        ConsensusStatusInfo statusInfo = consensusManager.getConsensusStatusInfo();
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

}
