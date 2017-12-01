package io.nuls.consensus.module.impl;

import io.nuls.consensus.constant.POCConsensusConstant;
import io.nuls.consensus.event.JoinConsensusEvent;
import io.nuls.consensus.handler.JoinConsensusHandler;
import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.service.impl.POCConsensusServiceImpl;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.NulsEventHeader;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.processor.service.intf.LocalProcessorService;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.ledger.entity.TransferTransaction;
import io.nuls.network.service.NetworkService;

import java.io.IOException;

/**
 *
 * @author Niels
 * @date 2017/11/7
 *
 */
//todo
public class POCConsensusModuleImpl extends AbstractConsensusModule {

    private NetworkProcessorService processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);

    @Override
    public void start() {
        this.checkGenesisBlock();
        this.checkBlockHeight();
        this.checkConsensusStatus();
        this.startBlockMaintenanceThread();
        this.registerHanders();
        this.registerService(POCConsensusServiceImpl.getInstance());
        Log.info("the POC consensus module is started!");
//        todo test network broadcast
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                while(true) {
//                    try {
//                        Thread.sleep(5000L);
//                    } catch (InterruptedException e) {
//                        Log.error(e);
//                    }
//                    try {
//                        JoinConsensusEvent event = new JoinConsensusEvent();
//                        event.setEventBody(new TransferTransaction<>());
//                        NulsContext.getInstance().getService(NetworkService.class).broadcast(event);
//                    } catch (Exception e) {
//                        Log.error(e);
//                    }
//                }
//            }
//        };
//        ThreadManager.asynExecuteRunnable(r);
    }

    private void registerHanders() {
        //todo
        JoinConsensusHandler joinConsensusHandler = new JoinConsensusHandler();
        //add filter , add validator
        processorService.registerEventHandler(JoinConsensusEvent.class,joinConsensusHandler);
    }

    private void checkConsensusStatus() {
        //todo
    }

    private void checkBlockHeight() {
        //todo
    }

    private void startBlockMaintenanceThread() {
        //todo
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
}
