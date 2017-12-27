package io.nuls.consensus.handler;

import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.BlockHeaderCacheService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.Transaction;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.ledger.service.intf.LedgerService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class SmallBlockHandler extends AbstractEventBusHandler<SmallBlockEvent> {
    private BlockHeaderCacheService headerCacheService = BlockHeaderCacheService.getInstance();
    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private LedgerService ledgerService = NulsContext.getInstance().getService(LedgerService.class);

    @Override
    public void onEvent(SmallBlockEvent event, String fromId) {
        BlockHeader header = headerCacheService.getHeader(event.getEventBody().getBlockHash().getDigestHex());
        if (header == null) {
            return;
        }
        Block block = new Block();
        block.setHeader(header);
        List<Transaction> txs = new ArrayList<>();
        for (NulsDigestData txHash : header.getTxHashList()) {
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
