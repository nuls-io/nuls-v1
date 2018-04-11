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

import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.constant.DownloadStatus;
import io.nuls.consensus.constant.MaintenanceStatus;
import io.nuls.consensus.manager.BlockManager;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.service.intf.DownloadService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.network.service.NetworkService;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class BlockPersistenceThread implements Runnable {
    public static final String THREAD_NAME = "block-persistence-thread";
    private static final BlockPersistenceThread INSTANCE = new BlockPersistenceThread();
    private BlockManager blockManager = BlockManager.getInstance();
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private DownloadService downloadService  = NulsContext.getServiceBean(DownloadService.class);
    private ConfirmingTxCacheManager txCacheManager = ConfirmingTxCacheManager.getInstance();
    private boolean running;

    private BlockPersistenceThread() {
    }

    public static final BlockPersistenceThread getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        if (this.running) {
            return;
        }
        this.running = true;
        while (true) {
            try {
                long height = blockManager.getStoredHeight() + 1;
                if (height == 1) {
                    height = blockService.getLocalSavedHeight() + 1;
                }
                boolean success = blockManager.processingBifurcation(height);
                if (success&&this.downloadService.getStatus()!= DownloadStatus.DOWNLOADING) {
                    doPersistence(height);
                } else {
                    Thread.sleep(5000L);
                }
            } catch (Exception e) {
                Log.error(e);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e1) {
                    Log.error(e1);
                }
            }
        }
    }

    private void doPersistence(long height) throws IOException {
        Block block = blockManager.getBlock(height);
        if (null == block) {
            BlockMaintenanceThread.getInstance().setStatus(MaintenanceStatus.READY);
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR,"the block shouldn't be null!height:"+height);
        }
        if (block.getTxs().isEmpty()) {
            //todo why
            Log.warn("block has no tx!");
            ConsensusManager.getInstance().destroy();
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR,"the block shouldn't be null!height:"+height);
        }
        boolean isSuccess;
        try {
            isSuccess = blockService.saveBlock(block);
        } catch (Exception e) {
            Log.error(e);
            ConsensusManager.getInstance().clearCache();
            NulsContext.getInstance().setBestBlock(blockService.getBlock(blockManager.getStoredHeight()));
            isSuccess = false;
        }
        if (isSuccess) {
            blockManager.storedBlock(block);
            txCacheManager.removeTxList(block.getTxHashList());
        }
    }

}
