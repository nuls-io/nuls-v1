/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.block.BestCorrectBlock;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.BlockBatchDownloadUtils;
import io.nuls.consensus.utils.BlockInfo;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.queue.entity.BlockingQueueImpl;
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/11/10
 */
public class BlockMaintenanceThread implements Runnable {

    public static DistributedBlockInfoRequestUtils BEST_HEIGHT_FROM_NET = DistributedBlockInfoRequestUtils.getInstance();

    public static final String THREAD_NAME = "block-maintenance";

    private static BlockMaintenanceThread instance = new BlockMaintenanceThread();
    private LedgerService ledgerService = NulsContext.getServiceBean(LedgerService.class);
    private final BlockService blockService = NulsContext.getServiceBean(BlockService.class);

    private BlockMaintenanceThread() {
    }

    public static synchronized BlockMaintenanceThread getInstance() {
        return instance;
    }

    @Override
    public void run() {
        try {
            checkGenesisBlock();
        } catch (Exception e) {
            Log.error(e);
        }
        while (true) {
            try {
                syncBlock();
                Thread.sleep(PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L);
            } catch (NulsRuntimeException e1) {
                Log.warn(e1.getMessage());
                try {
                    Thread.sleep(PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L);
                } catch (InterruptedException e2) {
                    Log.error(e2);
                }
            } catch (Exception e) {
                Log.error(e);
                try {
                    Thread.sleep(PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 1000L);
                } catch (InterruptedException e2) {
                    Log.error(e2);
                }
            }

        }
    }

    public synchronized void syncBlock() {
        BestCorrectBlock bestCorrectBlock = getBestCorrectBlock();
        boolean doit = false;
        long startHeight = 1;
        do {
            if (null == bestCorrectBlock.getLocalBestBlock() && bestCorrectBlock.getNetBestBlockInfo() == null) {
                doit = true;
                BlockInfo blockInfo = BEST_HEIGHT_FROM_NET.request(-1, null);
                bestCorrectBlock.setNetBestBlockInfo(blockInfo);
                break;
            }
            startHeight = bestCorrectBlock.getLocalBestBlock().getHeader().getHeight() + 1;
            long interval = TimeService.currentTimeMillis() - bestCorrectBlock.getLocalBestBlock().getHeader().getTime();
            if (interval < (PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 2000)) {
                doit = false;
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
                break;
            }
            ;
            if (null == bestCorrectBlock.getNetBestBlockInfo()) {
                bestCorrectBlock.setNetBestBlockInfo(BEST_HEIGHT_FROM_NET.request(0, null));
            }
            if (null == bestCorrectBlock.getNetBestBlockInfo()) {
                break;
            }
            if (bestCorrectBlock.getNetBestBlockInfo().getBestHeight() > bestCorrectBlock.getLocalBestBlock().getHeader().getHeight()) {
                doit = true;
                break;
            }
        } while (false);
        if (null == bestCorrectBlock.getNetBestBlockInfo()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            return;
        }
        if (doit) {
            downloadBlocks(bestCorrectBlock.getNetBestBlockInfo().getNodeIdList(), startHeight, bestCorrectBlock.getNetBestBlockInfo().getBestHeight());
        }
    }


    private void downloadBlocks(List<String> nodeIdList, long startHeight, long endHeight) {
        BlockBatchDownloadUtils utils = BlockBatchDownloadUtils.getInstance();
        try {
            utils.request(nodeIdList, startHeight, endHeight);
        } catch (InterruptedException e) {
            Log.error(e);
        }
    }

    public void checkGenesisBlock() throws Exception {
        Block genesisBlock = NulsContext.getInstance().getGenesisBlock();
        ValidateResult result = genesisBlock.verify();
        if (result.isFailed()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, result.getMessage());
        }
        Block localGenesisBlock = this.blockService.getGengsisBlock();
        if (null == localGenesisBlock) {
            for (Transaction tx : genesisBlock.getTxs()) {
                ledgerService.approvalTx(tx);
            }
            this.blockService.saveBlock(genesisBlock);
            return;
        }
        localGenesisBlock.verify();
        String logicHash = genesisBlock.getHeader().getHash().getDigestHex();
        String localHash = localGenesisBlock.getHeader().getHash().getDigestHex();
        if (!logicHash.equals(localHash)) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }
    }

    private BestCorrectBlock getBestCorrectBlock() {
        BestCorrectBlock resultCorrentInfo = new BestCorrectBlock();
        Block localBestBlock = this.blockService.getLocalBestBlock();
        do {
            if (null == localBestBlock || localBestBlock.getHeader().getHeight() <= 1) {
                break;
            }
            BlockInfo netBestBlockInfo = DistributedBlockInfoRequestUtils.getInstance().request(0, null);
            resultCorrentInfo.setNetBestBlockInfo(netBestBlockInfo);
            if (null == netBestBlockInfo || netBestBlockInfo.getBestHash() == null) {
                break;
            }
            //same to network nodes
            if (netBestBlockInfo.getBestHeight() == localBestBlock.getHeader().getHeight() &&
                    netBestBlockInfo.getBestHash().equals(localBestBlock.getHeader().getHash())) {
                break;
            } else if (netBestBlockInfo.getBestHeight() <= localBestBlock.getHeader().getHeight()) {
                if (netBestBlockInfo.getBestHeight() == 0) {
                    break;
                }
                //local height is highest
                BlockHeader header = null;
                try {
                    header = blockService.getBlockHeader(netBestBlockInfo.getBestHeight());
                } catch (NulsException e) {
                    break;
                }

                if (null != header && header.getHash().equals(netBestBlockInfo.getBestHash())) {
                    break;
                }
                if (netBestBlockInfo.getNodeIdList().size() == 1) {
                    throw new NulsRuntimeException(ErrorCode.FAILED, "node count not enough!");
                }
                Log.warn("Rollback block start height:{},local is highest and wrong!", localBestBlock.getHeader().getHeight());
                //bifurcation
                rollbackBlock(localBestBlock.getHeader().getHeight(), netBestBlockInfo.getNodeIdList());
                localBestBlock = this.blockService.getLocalBestBlock();
                break;
            } else {
                checkNeedRollback(localBestBlock, netBestBlockInfo.getNodeIdList(), netBestBlockInfo);
                localBestBlock = this.blockService.getLocalBestBlock();
            }
        } while (false);
        resultCorrentInfo.setLocalBestBlock(localBestBlock);
        return resultCorrentInfo;
    }

    private void checkNeedRollback(Block block, List<String> nodeIdList, BlockInfo netBestBlockInfo) {
        BlockInfo netThisBlockInfo = DistributedBlockInfoRequestUtils.getInstance().request(block.getHeader().getHeight(), nodeIdList);
        if (netThisBlockInfo.getBestHash().equals(block.getHeader().getHash())) {
            return;
        }
        if (block.getHeader().getHeight() != netThisBlockInfo.getBestHeight()) {
            throw new NulsRuntimeException(ErrorCode.FAILED, "answer not asked!");
        }
        Log.warn("Rollback block start height:{},local has wrong blocks!", block.getHeader().getHeight());
        //bifurcation
        rollbackBlock(block.getHeader().getHeight(), nodeIdList);
    }

    private void rollbackBlock(long startHeight, List<String> nodeIdList) {
        try {
            this.blockService.rollbackBlock(startHeight);
            long height = startHeight - 1;
            Block block = this.blockService.getBlock(height);
            if (null == block) {
                block = this.blockService.getLocalBestBlock();
            }
            NulsContext.getInstance().setBestBlock(block);
            checkNeedRollback(block, nodeIdList, null);
        } catch (NulsException e) {
            Log.error(e);
            return;
        }
    }
}
