package io.nuls.consensus.thread;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.utils.ConsensusBeanUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.ConsensusDao;
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
    private ConsensusDao consensusDao = NulsContext.getInstance().getService(ConsensusDao.class);
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
            BlockPo blockPo = ConsensusBeanUtils.toPojo(block);
            List<TransactionPo> txPoList = new ArrayList<>();
            for (int x = 0; x < block.getHeader().getTxCount(); x++) {
                Transaction tx = block.getTxs().get(x);
                try {
                    tx.onCommit();
                    txPoList.add(ConsensusBeanUtils.toPojo(tx));
                } catch (NulsException e) {
                    Log.error(e);
                    rollback(block.getTxs(), x);
                    throw new NulsRuntimeException(e);
                }
            }
            this.consensusDao.blockPersistence(blockPo, txPoList);
            this.blockCacheService.removeBlock(blockCacheService.getMinHeight());
        }
    }

    private void rollback(List<Transaction> txs, int max) {
        for (int x = 0; x < max; x++) {
            Transaction tx = txs.get(x);
            try {
                tx.onRollback();
            } catch (NulsException e) {
                Log.error(e);
            }
        }

    }
}
