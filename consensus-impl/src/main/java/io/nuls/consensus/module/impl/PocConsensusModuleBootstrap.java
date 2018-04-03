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
package io.nuls.consensus.module.impl;

import io.nuls.consensus.constant.ConsensusStatusEnum;
import io.nuls.consensus.entity.ConsensusStatusInfo;
import io.nuls.consensus.entity.tx.*;
import io.nuls.consensus.entity.validator.PocBlockValidatorManager;
import io.nuls.consensus.event.*;
import io.nuls.consensus.event.handler.*;
import io.nuls.consensus.event.notice.*;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.module.AbstractConsensusModule;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.impl.PocConsensusServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.service.tx.*;
import io.nuls.consensus.thread.BlockCacheCheckThread;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.constant.TransactionConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.EventManager;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.ledger.event.TransactionEvent;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class PocConsensusModuleBootstrap extends AbstractConsensusModule {

    private EventBusService eventBusService = NulsContext.getServiceBean(EventBusService.class);

    private ConsensusManager consensusManager = ConsensusManager.getInstance();

    @Override
    public void init() {
        EventManager.putEvent(AssembledBlockNotice.class);
        EventManager.putEvent(CancelConsensusNotice.class);
        EventManager.putEvent(EntrustConsensusNotice.class);
        EventManager.putEvent(PackedBlockNotice.class);
        EventManager.putEvent(RegisterAgentNotice.class);
        EventManager.putEvent(StopConsensusNotice.class);
        PocBlockValidatorManager.initHeaderValidators();
        PocBlockValidatorManager.initBlockValidators();
        this.registerTransaction(TransactionConstant.TX_TYPE_REGISTER_AGENT, RegisterAgentTransaction.class, RegisterAgentTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_RED_PUNISH, RedPunishTransaction.class, RedPunishTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_YELLOW_PUNISH, YellowPunishTransaction.class, YellowPunishTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_JOIN_CONSENSUS, PocJoinConsensusTransaction.class,JoinConsensusTxService.class);
        this.registerTransaction(TransactionConstant.TX_TYPE_EXIT_CONSENSUS, PocExitConsensusTransaction.class,ExitConsensusTxService.class);
        this.registerService(BlockServiceImpl.class);
        this.registerService(PocConsensusServiceImpl.class);
    }

    @Override
    public void start() {
        consensusManager.init();
        this.registerHandlers();
        this.consensusManager.startMaintenanceWork();
        consensusManager.joinConsensusMeeting();
        consensusManager.startPersistenceWork();

        Log.info("the POC consensus module is started!");
        TaskManager.createAndRunThread(this.getModuleId(),"block-cache-check",new BlockCacheCheckThread());
    }


    private void registerHandlers() {
        BlockEventHandler blockEventHandler = new BlockEventHandler();
        eventBusService.subscribeEvent(BlockEvent.class, blockEventHandler);

        BlockHeaderHandler blockHeaderHandler = new BlockHeaderHandler();
        eventBusService.subscribeEvent(BlockHeaderEvent.class, blockHeaderHandler);

        GetBlockHandler getBlockHandler = new GetBlockHandler();
        eventBusService.subscribeEvent(GetBlockRequest.class, getBlockHandler);

        GetTxGroupHandler getTxGroupHandler = new GetTxGroupHandler();
        eventBusService.subscribeEvent(GetTxGroupRequest.class, getTxGroupHandler);

        GetBlockHeaderHandler getBlockHeaderHandler = new GetBlockHeaderHandler();
        eventBusService.subscribeEvent(GetBlockHeaderEvent.class, getBlockHeaderHandler);

        TxGroupHandler txGroupHandler = new TxGroupHandler();
        eventBusService.subscribeEvent(TxGroupEvent.class, txGroupHandler);

        NewTxEventHandler newTxEventHandler = NewTxEventHandler.getInstance();
        eventBusService.subscribeEvent(TransactionEvent.class, newTxEventHandler);


        eventBusService.subscribeEvent(BlocksHashEvent.class, new BlocksHashHandler());
        eventBusService.subscribeEvent(GetBlocksHashRequest.class, new GetBlocksHashHandler());
        eventBusService.subscribeEvent(GetSmallBlockRequest.class, new GetSmallBlockHandler());
        eventBusService.subscribeEvent(SmallBlockEvent.class, new SmallBlockHandler());
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
