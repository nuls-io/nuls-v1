package io.nuls.consensus.service.impl;

import io.nuls.consensus.utils.ConsensusTool;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.BlockHeaderService;
import io.nuls.db.entity.BlockHeaderPo;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2018/1/10
 */
public class BlockStorageService {
    private static final BlockStorageService INSTANCE = new BlockStorageService();

    private BlockHeaderService headerDao = NulsContext.getInstance().getService(BlockHeaderService.class);
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    private BlockStorageService() {
    }

    public static BlockStorageService getInstance() {
        return INSTANCE;
    }

    public Block getBlock(long height) throws Exception {
        BlockHeader header = getBlockHeader(height);
        List<Transaction> txList = null;
        try {
            txList = ledgerService.getListByHeight(height);
        } catch (Exception e) {
            Log.error(e);
        }
        return fillBlock(header, txList);
    }

    public Block getBlock(String hash) throws Exception {
        BlockHeader header = getBlockHeader(hash);
        List<Transaction> txList = null;
        try {
            txList = ledgerService.getListByHeight(header.getHeight());
        } catch (Exception e) {
            Log.error(e);
        }
        return fillBlock(header, txList);
    }

    private Block fillBlock(BlockHeader header, List<Transaction> txList) {
        Block block = new Block();
        block.setTxs(txList);
        block.setHeader(header);
//        ValidateResult result = block.verify();
//        if (result.isFailed()) {
//            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
//        }
        return block;
    }


    public List<Block> getBlock(long startHeight, long endHeight) throws Exception {
        List<Block> blockList = new ArrayList<>();
        List<BlockHeaderPo> poList = headerDao.getHeaderList(startHeight, endHeight);
        List<Transaction> txList = null;
        try {
            txList = ledgerService.getListByHeight(startHeight, endHeight);
        } catch (Exception e) {
            Log.error(e);
        }
        Map<Long, List<Transaction>> txListGroup = txListGrouping(txList);
        for (BlockHeaderPo po : poList) {
            BlockHeader header = ConsensusTool.fromPojo(po);
            blockList.add(fillBlock(header, txListGroup.get(header.getHeight())));
        }
        return blockList;
    }

    private Map<Long, List<Transaction>> txListGrouping(List<Transaction> txList) {
        Map<Long, List<Transaction>> map = new HashMap<>();

        for (Transaction tx : txList) {
            List<Transaction> list = map.get(tx.getBlockHeight());
            if (null == list) {
                list = new ArrayList<>();
            }
            list.add(tx);
            map.put(tx.getBlockHeight(), list);
        }
        return map;
    }

    public BlockHeader getBlockHeader(long height) {
        BlockHeaderPo po = this.headerDao.getHeader(height);
        return ConsensusTool.fromPojo(po);
    }

    public BlockHeader getBlockHeader(String hash) {
        BlockHeaderPo po = this.headerDao.getHeader(hash);
        return ConsensusTool.fromPojo(po);
    }

    public long getBestHeight() {
        return headerDao.getBestHeight();
    }

    public void save(BlockHeader header) {
        headerDao.save(ConsensusTool.toPojo(header));
    }

    public void delete(String hash) {
        headerDao.delete(hash);
    }

    public int getCount(String address, long roundStart, long roundEnd) {
        return headerDao.getCount(address,roundStart,roundEnd);
    }
}
