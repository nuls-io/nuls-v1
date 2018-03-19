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

import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class BlockPersistenceThread implements Runnable {
    public static final String THREAD_NAME = "block-persistence-thread";
    private static final BlockPersistenceThread INSTANCE = new BlockPersistenceThread();
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
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
                doPersistence();
                if (blockCacheManager.canPersistence()) {
                    continue;
                }
                Thread.sleep(1000L);
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }

    private void doPersistence() throws IOException {
        long height = blockCacheManager.getStoredHeight() + 1;
        if (height == 1) {
            height = blockService.getLocalSavedHeight() + 1;
        }
        if ((height + PocConsensusConstant.CONFIRM_BLOCK_COUNT) >= blockCacheManager.getBestHeight()) {
            return;
        }
        Block block = blockCacheManager.getBlock(height);
        if (null == block) {
            return;
        }
        boolean isSuccess = blockService.saveBlock(block);
        if (isSuccess) {
            blockCacheManager.removeBlock(block.getHeader());
            blockCacheManager.setStoredHeight(height);
            txCacheManager.removeTxList(block.getTxHashList());
        }
    }

}
