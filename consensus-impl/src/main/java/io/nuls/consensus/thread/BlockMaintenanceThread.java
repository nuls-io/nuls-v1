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
package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
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
import io.nuls.core.validate.ValidateResult;
import io.nuls.ledger.service.intf.LedgerService;

import java.io.IOException;
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
    private boolean success = false;

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

        Block localBestBlock = getLocalBestCorrectBlock();
        boolean doit = false;
        long startHeight = 1;
        BlockInfo blockInfo = null;
        do {
            if (null == localBestBlock) {
                doit = true;
                this.success = false;
                blockInfo = BEST_HEIGHT_FROM_NET.request(-1);
                break;
            }
            startHeight = localBestBlock.getHeader().getHeight() + 1;
            long interval = TimeService.currentTimeMillis() - localBestBlock.getHeader().getTime();
            if (interval < (PocConsensusConstant.BLOCK_TIME_INTERVAL_SECOND * 2000)) {
                doit = false;
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
                break;
            }
            blockInfo = BEST_HEIGHT_FROM_NET.request(0);
            if (null == blockInfo) {
                this.success = false;
                break;
            }
            if (blockInfo.getBestHeight() > localBestBlock.getHeader().getHeight()) {
                this.success = false;
                doit = true;
                break;
            }
        } while (false);
        if (null == blockInfo) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            return;
        }
        if (doit) {
            downloadBlocks(blockInfo.getNodeIdList(), startHeight, blockInfo.getBestHeight());
        }
        this.success = true;
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

    private Block getLocalBestCorrectBlock() {
        Block localBestBlock = this.blockService.getLocalBestBlock();
        do {
            if (null == localBestBlock || localBestBlock.getHeader().getHeight() <= 1) {
                break;
            }
            BlockInfo blockInfo = DistributedBlockInfoRequestUtils.getInstance().request(0);
            if (null == blockInfo || blockInfo.getBestHash() == null) {
                break;
            }
            //same to network nodes
            if (blockInfo.getBestHeight() == localBestBlock.getHeader().getHeight() &&
                    blockInfo.getBestHash().equals(localBestBlock.getHeader().getHash())) {
                break;
            } else if (blockInfo.getBestHeight() <= localBestBlock.getHeader().getHeight()) {
                if (blockInfo.getBestHeight() == 0 || blockInfo.getNodeIdList().size() == 1) {
                    break;
                }
                //local height is highest
                BlockHeader header = null;
                try {
                    header = blockService.getBlockHeader(blockInfo.getBestHeight());
                } catch (NulsException e) {
                    break;
                }

                if (null != header && header.getHash().equals(blockInfo.getBestHash())) {
                    break;
                }
                Log.warn("Rollback block start height:{},local is highest and wrong!", localBestBlock.getHeader().getHeight());
                //bifurcation
                rollbackBlock(localBestBlock.getHeader().getHeight());
                localBestBlock = this.blockService.getLocalBestBlock();
                break;
            } else {
                blockInfo = DistributedBlockInfoRequestUtils.getInstance().request(localBestBlock.getHeader().getHeight());
                if (blockInfo.getBestHash().equals(localBestBlock.getHeader().getHash())) {
                    break;
                }
                if(localBestBlock.getHeader().getHeight()!=blockInfo.getBestHeight()){
                    throw new NulsRuntimeException(ErrorCode.FAILED,"answer not asked!");
                }
                if (blockInfo.getNodeIdList().size() == 1) {
                    throw new NulsRuntimeException(ErrorCode.FAILED, "node count not enough!");
                }
                Log.warn("Rollback block start height:{},local has wrong blocks!", localBestBlock.getHeader().getHeight());
                //bifurcation
                rollbackBlock(localBestBlock.getHeader().getHeight());
                localBestBlock = this.blockService.getLocalBestBlock();
            }
        } while (false);
        return localBestBlock;
    }

    private void rollbackBlock(long startHeight) {
        try {
            this.blockService.rollbackBlock(startHeight);
        } catch (NulsException e) {
            Log.error(e);
            return;
        }
        long height = startHeight - 1;
        boolean previousRb = false;
        if (height > 0) {
            Block block = this.blockService.getBlock(height);
            NulsContext.getInstance().setBestBlock(block);
            BlockInfo blockInfo = DistributedBlockInfoRequestUtils.getInstance().request(height);
            if (null != blockInfo && null != blockInfo.getBestHash() && !blockInfo.getBestHash().equals(block.getHeader().getHash())) {
                previousRb = true;
            }
        }
        if (previousRb) {
            rollbackBlock(height);
        } else {
            NulsContext.getInstance().setBestBlock(blockService.getLocalBestBlock());
        }
    }

    public boolean isSuccess() {
        return success;
    }
}
