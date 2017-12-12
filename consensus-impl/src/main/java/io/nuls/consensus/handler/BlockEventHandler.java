package io.nuls.consensus.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.ConsensusCacheService;
import io.nuls.consensus.utils.DistributedBlockDownloadUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class BlockEventHandler extends AbstractNetworkNulsEventHandler<BlockEvent> {

    private BlockCacheService blockCacheService = BlockCacheService.getInstance();

    @Override
    public void onEvent(BlockEvent event, String formId) throws NulsException {
        Block block = event.getEventBody();
        if (DistributedBlockDownloadUtils.getInstance().recieveBlock(formId, block)) {
            return;
        }
        try {
            block.verify();
        } catch (NulsRuntimeException e) {
            Log.error(e);
            return;
        }
        blockCacheService.cacheBlock(block);
    }
}
