package io.nuls.consensus.thread;

import io.nuls.consensus.cache.manager.tx.ReceivedTxCacheManager;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.service.impl.BlockServiceImpl;
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
    private ReceivedTxCacheManager txCacheManager = ReceivedTxCacheManager.getInstance();
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
        //todo
        long height = blockCacheManager.getStoredHeight()+1;
//        for (int i = 0; i < count; i++) {
//            Block block = blockCacheManager.getMinHeightCacheBlock();
//            if (null == block) {
//                throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
//            }
//
//            blockService.saveBlock(block);
//            this.blockCacheManager.removeCache(blockCacheManager.getMinHeight());
//            this.txCacheManager.removeTx(block.getTxHashList());
//        }
    }

}
