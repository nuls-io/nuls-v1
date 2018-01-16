package io.nuls.consensus.event.handler;

import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.event.bus.handler.AbstractEventHandler;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class BlockHeaderHandler extends AbstractEventHandler<BlockHeaderEvent> {

    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();

    @Override
    public void onEvent(BlockHeaderEvent event, String fromId) {
        BlockHeader header = event.getEventBody();
        blockCacheManager.cacheBlockHeader(header, fromId);

    }
}
