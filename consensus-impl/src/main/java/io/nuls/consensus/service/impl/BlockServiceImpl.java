package io.nuls.consensus.service.impl;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.consensus.entity.genesis.GenesisBlock;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.BlockDao;
import io.nuls.db.dao.ConsensusDao;
import io.nuls.db.entity.BlockPo;
import io.nuls.db.entity.TransactionPo;
import io.nuls.ledger.entity.TransactionTool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BlockServiceImpl implements BlockService {
    private static final BlockServiceImpl INSTANCE = new BlockServiceImpl();

    private BlockDao blockDao = NulsContext.getInstance().getService(BlockDao.class);
    private ConsensusDao consensusDao = NulsContext.getInstance().getService(ConsensusDao.class);
    private BlockServiceImpl() {
    }

    public static BlockServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Block getGengsisBlockFromDb() {
        BlockPo po = this.blockDao.getBlockByHeight(0);
        try {
            return ConsensusTool.fromPojo(po);
        } catch (NulsException e) {
            Log.error(e);
            return null;
        }
    }

    @Override
    public long getLocalHeight() {
        // todo auto-generated method stub(niels)
        return 0;
    }

    @Override
    public byte[] getLocalHighestHash() {
        // todo auto-generated method stub(niels)
        return new byte[0];
    }

    @Override
    public long getBestHeight() {
        // todo auto-generated method stub(niels)
        return 0;
    }

    @Override
    public byte[] getBestHash() {
        // todo auto-generated method stub(niels)
        return new byte[0];
    }

    @Override
    public Block getLocalHighestBlock() {
        // todo auto-generated method stub(niels)
        return GenesisBlock.getInstance();
    }

    @Override
    public Block getBestBlock() {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public Block getBlockByHash(String hash) {
        // todo auto-generated method stub(niels)
        return null;
    }

    @Override
    public Block getBlockByHeight(long height) {
        // todo auto-generated method stub(niels)
        return null;
    }


    @Override
    public void save(Block block) {
        BlockPo blockPo = ConsensusTool.toPojo(block);
        List<TransactionPo> txPoList = new ArrayList<>();
        for (int x = 0; x < block.getHeader().getTxCount(); x++) {
            Transaction tx = block.getTxs().get(x);
            try {
                tx.onCommit();
                txPoList.add(TransactionTool.toPojo(tx));
            } catch (Exception e) {
                Log.error(e);
                rollback(block.getTxs(), x);
                throw new NulsRuntimeException(e);
            }
        }
        consensusDao.blockPersistence(blockPo,txPoList);
    }

    @Override
    public void clearLocalBlocks() {
        // todo auto-generated method stub(niels)

    }

    @Override
    public void rollback(long height) {
        // todo auto-generated method stub(niels)
        //删除关联数据及区块数据
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
