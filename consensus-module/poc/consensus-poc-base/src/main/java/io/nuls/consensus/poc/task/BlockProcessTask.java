/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.task;

import io.nuls.consensus.poc.container.BlockContainer;
import io.nuls.consensus.poc.locker.Lockers;
import io.nuls.consensus.poc.process.BlockProcess;
import io.nuls.consensus.poc.constant.BlockContainerStatus;
import io.nuls.consensus.poc.constant.ConsensusStatus;
import io.nuls.consensus.poc.provider.BlockQueueProvider;
import io.nuls.consensus.poc.context.ConsensusStatusContext;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.NulsConstant;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Transaction;
import io.nuls.ledger.service.LedgerService;
import io.nuls.protocol.service.DownloadService;
import io.nuls.protocol.service.TransactionService;

import java.util.List;

/**
 * @author ln
 */
public class BlockProcessTask implements Runnable {

    private DownloadService downloadService = NulsContext.getServiceBean(DownloadService.class);

    private BlockProcess blockProcess;
    private BlockQueueProvider blockQueueProvider = BlockQueueProvider.getInstance();

    private boolean first = true;

    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);

    private TransactionService transactionService = NulsContext.getServiceBean(TransactionService.class);

    public BlockProcessTask(BlockProcess blockProcess) {
        this.blockProcess = blockProcess;
    }

    @Override
    public void run() {
        Lockers.CHAIN_LOCK.lock();
        try {
            doTask();
        } catch (Exception e) {
            Log.error(e);
        } catch (Error e) {
            Log.error(e);
        } catch (Throwable e) {
            Log.error(e);
        } finally {
            Lockers.CHAIN_LOCK.unlock();
        }
    }

    private void doTask() {

        //wait consensus ready running
        if (ConsensusStatusContext.getConsensusStatus().ordinal() <= ConsensusStatus.LOADING_CACHE.ordinal()) {
            return;
        }
        BlockContainer blockContainer;
        while ((blockContainer = blockQueueProvider.get()) != null) {
            try {
                if (first) {
                    List<Transaction> txList = blockContainer.getBlock().getTxs();
                    for (int index = txList.size() - 1; index >= 0; index--) {
                        Transaction tx = blockContainer.getBlock().getTxs().get(index);
                        Transaction localTx = ledgerService.getTx(tx.getHash());

                        if (null == localTx || tx.getBlockHeight() != localTx.getBlockHeight()) {
                            continue;
                        }
                        try {
                            transactionService.rollbackTx(tx, blockContainer.getBlock().getHeader());
                        } catch (Exception e) {
                            Log.error(e);
                        }
                    }
                    first = false;
                }
                //long time = System.currentTimeMillis();
                blockProcess.addBlock(blockContainer);
                //Log.info("add 区块 " + blockContainer.getBlock().getHeader().getHeight() + " 耗时 " + (System.currentTimeMillis() - time) + " ms , tx count : " + blockContainer.getBlock().getHeader().getTxCount());
            } catch (Exception e) {
                e.printStackTrace();
                Log.error("add block fail , error : " + e.getMessage(), e);
            }
        }

        // The system starts up. The local height and the network height are the same. When the block is not to be downloaded, the system needs to know and set the consensus status to running.
        // 系统启动，本地高度和网络高度一致，不需要下载区块时，系统需要知道并设置共识状态为运行中
        if (downloadService.isDownloadSuccess().isSuccess() && ConsensusStatusContext.getConsensusStatus() == ConsensusStatus.WAIT_RUNNING &&
                (blockContainer == null || blockContainer.getStatus() == BlockContainerStatus.RECEIVED)) {
            ConsensusStatusContext.setConsensusStatus(ConsensusStatus.RUNNING);
            NulsContext.WALLET_STATUS = NulsConstant.RUNNING;
        }
    }
}
