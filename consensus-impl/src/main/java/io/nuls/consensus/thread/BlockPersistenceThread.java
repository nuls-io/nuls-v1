package io.nuls.consensus.thread;

import io.nuls.consensus.cache.manager.tx.ConfirmingTxCacheManager;
import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class BlockPersistenceThread implements Runnable {
    public static final String THREAD_NAME = "block-persistence-thread";
    private static final BlockPersistenceThread INSTANCE = new BlockPersistenceThread();
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();
    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);
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
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }

    private void doPersistence() {
        long height = blockCacheManager.getStoredHeight() + 1;
        Block block = blockCacheManager.getBlock(height);
        if (null == block) {
            return;
        }
        //todo
//        blockService.saveBlock(block);
//        blockCacheManager.removeBlock(height);
//        blockCacheManager.setStoredHeight(height);
//        txCacheManager.removeTxList(block.getTxHashList());
    }

}
