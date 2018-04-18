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
package io.nuls.protocol.base.module.impl;


import io.nuls.consensus.poc.protocol.context.ConsensusContext;
import io.nuls.consensus.poc.protocol.service.BlockService;
import io.nuls.consensus.poc.protocol.service.DownloadService;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.base.download.DownloadServiceImpl;
import io.nuls.protocol.base.handler.BlockEventHandler;
import io.nuls.protocol.base.handler.BlocksHashHandler;
import io.nuls.protocol.base.handler.GetBlocksHashHandler;
import io.nuls.protocol.base.handler.NotFoundHander;
import io.nuls.protocol.base.service.impl.BlockServiceImpl;
import io.nuls.protocol.base.service.impl.BlockStorageService;
import io.nuls.protocol.base.service.impl.SystemServiceImpl;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.BlockEvent;
import io.nuls.protocol.event.BlocksHashEvent;
import io.nuls.protocol.event.GetBlocksHashRequest;
import io.nuls.protocol.event.NotFoundEvent;
import io.nuls.protocol.model.Block;
import io.nuls.protocol.model.Transaction;
import io.nuls.protocol.module.AbstractProtocolModule;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class BaseProtocolsModuleBootstrap extends AbstractProtocolModule {

    private EventBusService eventBusService = NulsContext.getServiceBean(EventBusService.class);

    @Override
    public void init() {
        this.waitForDependencyInited(NulsConstant.MODULE_ID_CACHE,NulsConstant.MODULE_ID_DB,NulsConstant.MODULE_ID_EVENT_BUS);
        ConsensusContext.initConfiguration();
        this.registerService(BlockServiceImpl.class);
        Block bestBlock = null;
        try {
            BlockStorageService service = BlockStorageService.getInstance();
            bestBlock = service.getBlock(service.getBestHeight());
        } catch (Exception e) {
            Log.error(e);
        }
        if (null != bestBlock) {
            NulsContext.getInstance().setBestBlock(bestBlock);
        }
        this.registerService(SystemServiceImpl.class);
        this.registerService(DownloadServiceImpl.class);
    }

    @Override
    public void start() {
        this.checkGenesisBlock();


        this.initHandlers();
        NulsContext.getServiceBean(DownloadService.class).start();
        Log.info("the protocol module is started!");
    }

    public void checkGenesisBlock() {
        Block genesisBlock = NulsContext.getInstance().getGenesisBlock();
        ValidateResult result = genesisBlock.verify();
        if (result.isFailed()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, result.getMessage());
        }
        BlockService blockService = NulsContext.getServiceBean(BlockService.class);
//        LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
        Block localGenesisBlock = blockService.getGengsisBlock();
        if (null == localGenesisBlock) {
//            for (Transaction tx : genesisBlock.getTxs()) {
//                ledgerService.approvalTx(tx, genesisBlock);
//            }
            try {
                blockService.saveBlock(genesisBlock);
            } catch (IOException e) {
                throw new NulsRuntimeException(e);
            }
            return;
        }
        localGenesisBlock.verify();
        String logicHash = genesisBlock.getHeader().getHash().getDigestHex();
        String localHash = localGenesisBlock.getHeader().getHash().getDigestHex();
        if (!logicHash.equals(localHash)) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
    }

    private void initHandlers() {
        BlockEventHandler blockEventHandler = new BlockEventHandler();
        eventBusService.subscribeEvent(BlockEvent.class, blockEventHandler);
        eventBusService.subscribeEvent(BlocksHashEvent.class, new BlocksHashHandler());
        eventBusService.subscribeEvent(GetBlocksHashRequest.class, new GetBlocksHashHandler());

        eventBusService.subscribeEvent(NotFoundEvent.class, new NotFoundHander());
    }


    @Override
    public void shutdown() {
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
