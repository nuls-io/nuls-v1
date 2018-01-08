package io.nuls.consensus.event.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.utils.DistributedBlockDownloadUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.network.service.NetworkService;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class BlockEventHandler extends AbstractEventHandler<BlockEvent> {

    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();
    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);

    @Override
    public void onEvent(BlockEvent event, String fromId) {
        Block block = event.getEventBody();
        if (DistributedBlockDownloadUtils.getInstance().recieveBlock(fromId, block)) {
            return;
        }
        ValidateResult result = block.verify();
        if (null==result||result.isFailed()) {
             this.networkService.removeNode(fromId);
            return;
        }
        blockCacheManager.cacheBlock(block);
    }
}
