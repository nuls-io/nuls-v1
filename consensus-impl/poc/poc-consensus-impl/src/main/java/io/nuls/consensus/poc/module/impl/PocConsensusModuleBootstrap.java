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
 */
package io.nuls.consensus.poc.module.impl;

import io.nuls.consensus.poc.block.validator.PocBlockValidatorManager;
import io.nuls.consensus.poc.handler.*;
import io.nuls.consensus.poc.module.AbstractPocConsensusModule;
import io.nuls.consensus.poc.protocol.event.notice.*;
import io.nuls.consensus.poc.protocol.tx.*;
import io.nuls.consensus.poc.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.poc.service.impl.tx.*;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.protocol.base.handler.GetBlockHandler;
import io.nuls.protocol.base.handler.GetTxGroupHandler;
import io.nuls.protocol.base.handler.NewTxEventHandler;
import io.nuls.protocol.base.handler.TxGroupHandler;
import io.nuls.protocol.constant.TransactionConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.*;
import io.nuls.protocol.event.manager.EventManager;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.service.intf.TransactionService;
import io.nuls.protocol.utils.TransactionManager;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class PocConsensusModuleBootstrap extends AbstractPocConsensusModule {

    private EventBusService eventBusService = NulsContext.getServiceBean(EventBusService.class);

    @Override
    public void init() {
        EventManager.putEvent(AssembledBlockNotice.class);
        EventManager.putEvent(CancelConsensusNotice.class);
        EventManager.putEvent(EntrustConsensusNotice.class);
        EventManager.putEvent(PackedBlockNotice.class);
        EventManager.putEvent(RegisterAgentNotice.class);
        EventManager.putEvent(StopConsensusNotice.class);

        this.registerTransaction(TransactionConstant.TX_TYPE_REGISTER_AGENT, RegisterAgentTransaction.class, RegisterAgentTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_RED_PUNISH, RedPunishTransaction.class, RedPunishTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_YELLOW_PUNISH, YellowPunishTransaction.class, YellowPunishTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_JOIN_CONSENSUS, PocJoinConsensusTransaction.class,JoinConsensusTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_STOP_AGENT, StopAgentTransaction.class,StopAgentTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_CANCEL_DEPOSIT, CancelDepositTransaction.class,CancelDepositTxService.class);

        this.waitForDependencyInited(NulsConstant.MODULE_ID_PROTOCOL);
        this.registerService(PocConsensusServiceImpl.class);

        PocBlockValidatorManager.initValidators();
    }

    protected final void registerTransaction(int txType, Class<? extends Transaction> txClass, Class<? extends TransactionService> txServiceClass) {
        this.registerService(txServiceClass);
        TransactionManager.putTx(txType, txClass, txServiceClass);
    }
    @Override
    public void start() {
        this.waitForDependencyRunning(NulsConstant.MODULE_ID_PROTOCOL);
        try {
            NulsContext.getServiceBean(PocConsensusServiceImpl.class).startup();
        } catch (Exception e) {
            Log.error(e);
        }

        this.registerHandlers();
        Log.info("the POC consensus module is started!");
    }


    private void registerHandlers() {


        GetBlockHandler getBlockHandler = new GetBlockHandler();
        eventBusService.subscribeEvent(GetBlockRequest.class, getBlockHandler);

        GetTxGroupHandler getTxGroupHandler = new GetTxGroupHandler();
        eventBusService.subscribeEvent(GetTxGroupRequest.class, getTxGroupHandler);

        TxGroupHandler txGroupHandler = new TxGroupHandler();
        eventBusService.subscribeEvent(TxGroupEvent.class, txGroupHandler);

        NewTxEventHandler newTxEventHandler = NewTxEventHandler.getInstance();
        eventBusService.subscribeEvent(TransactionEvent.class, newTxEventHandler);

        SmallBlockHandler newBlockHandler = new SmallBlockHandler();
        eventBusService.subscribeEvent(SmallBlockEvent.class, newBlockHandler);
    }




    @Override
    public void shutdown() {
        TaskManager.shutdownByModuleId(this.getModuleId());
    }

    @Override
    public void destroy() {
        NulsContext.getServiceBean(PocConsensusServiceImpl.class).shutdown();
    }

    @Override
    public String getInfo() {
        if (this.getStatus() == ModuleStatusEnum.UNINITIALIZED || this.getStatus() == ModuleStatusEnum.INITIALIZING) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        str.append("module:[consensus]:\n");
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

}
