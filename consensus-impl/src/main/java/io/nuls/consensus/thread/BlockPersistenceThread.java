package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.ConsensusDao;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class BlockPersistenceThread implements Runnable {
    public static final String THREAD_NAME = "block-persistence-thread";
    private static final BlockPersistenceThread INSTANCE = new BlockPersistenceThread();
    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private ConsensusDao consensusDao = NulsContext.getInstance().getService(ConsensusDao.class);
    private boolean running;

    private BlockPersistenceThread() {
    }

    public static final BlockPersistenceThread getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        if(this.running ){
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
        long count = blockCacheService.getMaxHeight()-blockCacheService.getMinHeight()-PocConsensusConstant.CONFIRM_BLOCK_COUNT;
        // todo 检查缓存中的区块，将已确认的区块存入数据库，并且关联交易的存储及处理
        for(int i=0;i<count;i++){
            Block block = blockCacheService.getMinHeighBlock();
            if(null==block){
                throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
            }




//            this.consensusDao.blockPersistence();
        }
    }
}
