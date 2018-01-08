package io.nuls.consensus.service.impl;

import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.tx.serivce.CommonTransactionService;
import io.nuls.core.utils.log.Log;
import io.nuls.db.annotation.TransactionalAnnotation;
import io.nuls.db.dao.BlockDataService;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.db.util.TransactionPoTool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockServiceImpl implements BlockService {
    private static final BlockServiceImpl INSTANCE = new BlockServiceImpl();

    private BlockDataService blockDao = NulsContext.getInstance().getService(BlockDataService.class);
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();

    private CommonTransactionService txService = NulsContext.getInstance().getService(CommonTransactionService.class);

    private BlockServiceImpl() {
    }

    public static BlockServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Block getGengsisBlock() {
        BlockPo po = this.blockDao.getBlock(0);
        try {
            return ConsensusTool.fromPojo(po);
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public long getLocalHeight() {
        long height = blockCacheManager.getMaxHeight();
        if (height == 0) {
            height = blockDao.queryMaxHeight();
        }
        return height;
    }

    @Override
    public Block getLocalBestBlock() {
        Block block = blockCacheManager.getBlock(blockCacheManager.getMaxHeight());
        if (null == block) {
            BlockPo po = blockDao.getHighestBlock();
            try {
                block = ConsensusTool.fromPojo(po);
            } catch (NulsException e) {
                Log.error(e);
                return null;
            }
        }
        return block;
    }

    @Override
    public BlockHeader getBlockHeader() {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public Block getBlock(String hash) {
        Block block = blockCacheManager.getBlock(hash);
        if (null == block) {
            BlockPo po = blockDao.getBlockByHash(hash);
            try {
                block = ConsensusTool.fromPojo(po);
            } catch (NulsException e) {
                Log.error(e);
                return null;
            }
        }
        return block;
    }

    @Override
    public Block getBlock(long height) {
        Block block = blockCacheManager.getBlock(height);
        if (null == block) {
            BlockPo po = blockDao.getBlock(height);
            try {
                block = ConsensusTool.fromPojo(po);
            } catch (NulsException e) {
                Log.error(e);
                return null;
            }
        }
        return block;
    }


    @Override
    public void saveBlock(Block block) {
        BlockPo blockPo = ConsensusTool.toPojo(block);
        List<TransactionPo> txPoList = new ArrayList<>();
        for (int x = 0; x < block.getHeader().getTxCount(); x++) {
            Transaction tx = block.getTxs().get(x);
            tx.setBlockHash(block.getHeader().getHash());
            tx.setBlockHeight(block.getHeader().getHeight());
            try {
                txService.commit(tx);
                txPoList.add(TransactionPoTool.toPojo(tx));
            } catch (Exception e) {
                Log.error(e);
                rollback(block.getTxs(), x);
                throw new NulsRuntimeException(e);
            }
        }
        this.dataPersistence(blockPo, txPoList);
    }

    @TransactionalAnnotation
    private void dataPersistence(BlockPo blockPo, List<TransactionPo> txPoList) {
        //todo 调用多个dao/service进行
    }

    @Override
    public void rollbackBlock(long height) {
        Block block = this.getBlock(height);
        if (null == block) {
            return;
        }
        this.rollback(block.getTxs(), block.getTxs().size() - 1);
        blockDao.delete(block.getHeader().getHash().getDigestHex());
    }

    @Override
    public int getBlockCount(String address, long roundStart, long roundEnd) {
        return this.blockDao.count(address, roundStart, roundEnd);
    }

    private void rollback(List<Transaction> txs, int max) {
        for (int x = 0; x < max; x++) {
            Transaction tx = txs.get(x);
            try {
                txService.rollback(tx);
            } catch (NulsException e) {
                Log.error(e);
            }
        }

    }
}
