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
 *
 */
package io.nuls.protocol.base.module;


import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.kernel.utils.TransactionManager;
import io.nuls.message.bus.constant.MessageBusConstant;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.protocol.base.handler.*;
import io.nuls.protocol.base.service.DownloadServiceImpl;
import io.nuls.protocol.base.version.NulsVersionManager;
import io.nuls.protocol.message.*;
import io.nuls.protocol.model.tx.CoinBaseTransaction;
import io.nuls.protocol.model.tx.TransferTransaction;
import io.nuls.protocol.module.AbstractProtocolModule;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.DownloadService;

;

/**
 * @author Niels
 */
public class BaseProtocolsModuleBootstrap extends AbstractProtocolModule {

    @Override
    public void init() {
        TransactionManager.putTx(CoinBaseTransaction.class, null);
        TransactionManager.putTx(TransferTransaction.class, null);

        try {
            NulsVersionManager.init();
            NulsVersionManager.test();
            System.out.println(NulsVersionManager.getProtocolContainer(1));
        } catch (Exception e) {
            Log.error(e);
            System.exit(-1);
        }
    }

    @Override
    public void start() {
        this.waitForDependencyRunning(MessageBusConstant.MODULE_ID_MESSAGE_BUS);
        this.waitForDependencyInited(ConsensusConstant.MODULE_ID_CONSENSUS, NetworkConstant.NETWORK_MODULE_ID);
        BlockService blockService = NulsContext.getServiceBean(BlockService.class);
        Block block0 = blockService.getGengsisBlock().getData();
        Block genesisBlock = NulsContext.getInstance().getGenesisBlock();
        if (null == block0) {
            try {
                blockService.saveBlock(genesisBlock);
            } catch (NulsException e) {
                Log.error(e);
                throw new NulsRuntimeException(e);
            }
        }
        Block block = blockService.getBestBlock().getData();
        while (null != block && block.verify().isFailed()) {
            try {
                blockService.rollbackBlock(block);
            } catch (NulsException e) {
                Log.error(e);
            }
            block = blockService.getBlock(block.getHeader().getPreHash()).getData();
        }
        if (null != block) {
            NulsContext.getInstance().setBestBlock(block);
            this.initHandlers();
            ((DownloadServiceImpl) NulsContext.getServiceBean(DownloadService.class)).start();
        } else {
            start();
        }

    }

    private void initHandlers() {
        MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);
        messageBusService.subscribeMessage(BlockMessage.class, new BlockMessageHandler());
        messageBusService.subscribeMessage(BlocksHashMessage.class, new BlocksHashHandler());
        messageBusService.subscribeMessage(GetBlocksHashMessage.class, new GetBlocksHashHandler());
        messageBusService.subscribeMessage(NotFoundMessage.class, new NotFoundHander());
        messageBusService.subscribeMessage(GetBlockMessage.class, new GetBlockHandler());
        messageBusService.subscribeMessage(GetBlocksByHashMessage.class, new GetBlocksByHashHandler());
        messageBusService.subscribeMessage(GetBlocksByHeightMessage.class, new GetBlocksByHeightHandler());
        messageBusService.subscribeMessage(GetTxGroupRequest.class, new GetTxGroupHandler());
        messageBusService.subscribeMessage(TxGroupMessage.class, new TxGroupHandler());
        messageBusService.subscribeMessage(TransactionMessage.class, new TransactionMessageHandler());
        messageBusService.subscribeMessage(SmallBlockMessage.class, new SmallBlockHandler());
        messageBusService.subscribeMessage(CompleteMessage.class, new CompleteHandler());
        messageBusService.subscribeMessage(ReactMessage.class, new ReactMessageHandler());

//        TaskManager.createAndRunThread(ProtocolConstant.MODULE_ID_PROTOCOL, "Tx-Download", TransactionDownloadProcessor.getInstance());
        messageBusService.subscribeMessage(GetTxMessage.class, new GetTxMessageHandler());
//
//        TaskManager.createAndRunThread(ProtocolConstant.MODULE_ID_PROTOCOL, "SmallBlock-Download", SmallBlockDownloadProcessor.getInstance());
        messageBusService.subscribeMessage(GetSmallBlockMessage.class, new GetSmallBlockHandler());
        messageBusService.subscribeMessage(ForwardSmallBlockMessage.class, new ForwardSmallBlockHandler());
        messageBusService.subscribeMessage(ForwardTxMessage.class, new ForwardTxMessageHandler());
    }

    @Override
    public void shutdown() {
        ((DownloadServiceImpl) NulsContext.getServiceBean(DownloadService.class)).stop();
        TaskManager.shutdownByModuleId(this.getModuleId());
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getInfo() {
        return "";
    }

}
