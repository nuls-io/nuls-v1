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

import io.nuls.consensus.poc.manager.CacheManager;
import io.nuls.consensus.poc.module.AbstractPocConsensusModule;
import io.nuls.consensus.poc.service.impl.PocConsensusServiceImpl;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.ModuleStatusEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.BaseThread;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.service.intf.LedgerService;
import io.nuls.protocol.intf.BlockService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/7
 */
public class PocConsensusModuleBootstrap extends AbstractPocConsensusModule {

    @Override
    public void init() {
        this.registerService(PocConsensusServiceImpl.class);

    }

    @Override
    public void start() {
        try {
            checkGenesisBlock();
        } catch (Exception e) {
            Log.error(e);
        }

        try {
            NulsContext.getServiceBean(PocConsensusServiceImpl.class).startup();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.info("the POC consensus module is started!");
    }

    public void checkGenesisBlock() throws Exception {
        Block genesisBlock = NulsContext.getInstance().getGenesisBlock();
        ValidateResult result = genesisBlock.verify();
        if (result.isFailed()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, result.getMessage());
        }
        BlockService blockService = NulsContext.getServiceBean(BlockService.class);
        LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
        Block localGenesisBlock = blockService.getGengsisBlock();
        if (null == localGenesisBlock) {
            for (Transaction tx : genesisBlock.getTxs()) {
                ledgerService.approvalTx(tx, genesisBlock);
            }
            blockService.saveBlock(genesisBlock);
            return;
        }
        localGenesisBlock.verify();
        String logicHash = genesisBlock.getHeader().getHash().getDigestHex();
        String localHash = localGenesisBlock.getHeader().getHash().getDigestHex();
        if (!logicHash.equals(localHash)) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
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
