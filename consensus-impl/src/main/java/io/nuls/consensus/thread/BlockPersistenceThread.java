package io.nuls.consensus.thread;

import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class BlockPersistenceThread implements Runnable {
    public static final String THREAD_NAME = "block-persistence-thread";
    private static final BlockPersistenceThread INSTANCE = new BlockPersistenceThread();
    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private BlockPersistenceThread() {
    }

    public static final BlockPersistenceThread getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
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
        // todo 检查缓存中的区块，将已确认的区块存入数据库，并且关联交易的存储及处理

    }
}
