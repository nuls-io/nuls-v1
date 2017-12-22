package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.impl.BlockServiceImpl;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.ConsensusBeanUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.TransactionPo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class BlockPersistenceThread implements Runnable {
    public static final String THREAD_NAME = "block-persistence-thread";
    private static final BlockPersistenceThread INSTANCE = new BlockPersistenceThread();
    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private BlockService blockService = BlockServiceImpl.getInstance();
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
        long count = blockCacheService.getMaxHeight() - blockCacheService.getMinHeight() - PocConsensusConstant.CONFIRM_BLOCK_COUNT;
        for (int i = 0; i < count; i++) {
            Block block = blockCacheService.getMinHeighBlock();
            if (null == block) {
                throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
            }

            blockService.save(block);
            this.blockCacheService.removeBlock(blockCacheService.getMinHeight());
        }
    }

}
