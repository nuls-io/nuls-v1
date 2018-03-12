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
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.BlockBatchDownloadUtils;
import io.nuls.consensus.utils.BlockInfo;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;

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
        } catch (IOException e) {
            Log.error(e);
        }
        while (true) {
            try {
                syncBlock();
            } catch (NulsRuntimeException e1) {
                Log.error(e1.getMessage());
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

    public void checkGenesisBlock() throws IOException {
        Block genesisBlock = NulsContext.getInstance().getGenesisBlock();
        ValidateResult result = genesisBlock.verify();
        if (result.isFailed()) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR, result.getMessage());
        }
        Block localGenesisBlock = this.blockService.getGengsisBlock();
        if (null == localGenesisBlock) {
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
                return localBestBlock;
            }
            blockInfo = DistributedBlockInfoRequestUtils.getInstance().request(localBestBlock.getHeader().getHeight());
            if (null != blockInfo && blockInfo.getBestHeight() < localBestBlock.getHeader().getHeight()) {
                //本地高度最高，查询网络最新高度，并回退
                rollbackBlock(localBestBlock.getHeader().getHeight(), blockInfo);
                localBestBlock = this.blockService.getLocalBestBlock();
                break;
            }
            if (!blockInfo.getBestHash().equals(localBestBlock.getHeader().getHash())) {
                //本地分叉，回退
                rollbackBlock(blockInfo.getBestHeight(), blockInfo);
                localBestBlock = this.blockService.getLocalBestBlock();
                break;
            }
        } while (false);
        return localBestBlock;
    }

    private void rollbackBlock(long startHeight, BlockInfo blockInfo) {
        try {
            this.blockService.rollbackBlock(startHeight);
        } catch (NulsException e) {
            Log.error(e);
            return;
        }
        long height = startHeight - 1;
        if (height < 0) {
            return;
        }
        Block localBlock = this.blockService.getBlock(height);
        boolean previousRb = false;
        if (null == blockInfo || blockInfo.getBestHash() == null || localBlock == null || localBlock.getHeader().getHash() == null) {
            previousRb = true;
        } else if (!blockInfo.getBestHash().getDigestHex().equals(localBlock.getHeader().getHash().getDigestHex())) {
            previousRb = true;
        }
        if (previousRb) {
            rollbackBlock(height, blockInfo);
        }
    }

    public boolean isSuccess() {
        return success;
    }
}
