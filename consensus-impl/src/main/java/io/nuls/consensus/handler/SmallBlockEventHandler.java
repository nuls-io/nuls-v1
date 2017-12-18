package io.nuls.consensus.handler;

import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.BlockHeaderCacheService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class SmallBlockEventHandler extends AbstractNetworkNulsEventHandler<SmallBlockEvent> {
    private BlockHeaderCacheService headerCacheService = BlockHeaderCacheService.getInstance();
    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    @Override
    public void onEvent(SmallBlockEvent event, String fromId) throws NulsException {

        BlockHeader header = headerCacheService.getHeader(event.getEventBody().getHeight());
        if(header==null){
            return;
        }
        Block block = new Block();
        block.setHeader(header);
        List<Transaction> txs = new ArrayList<>();
        for(NulsDigestData txHash:header.getTxHashList()){
            Transaction tx = ledgerService.getTxFromCache(txHash.getDigestHex());
            txs.add(tx);
        }
        block.setTxs(txs);
        try {
            block.verify();
        } catch (NulsRuntimeException e) {
            Log.error(e);
            return;
        }
        blockCacheService.cacheBlock(block);
    }
}