package io.nuls.consensus.event.handler;

import io.nuls.consensus.cache.manager.block.BlockCacheManager;
import io.nuls.consensus.event.SmallBlockEvent;
import io.nuls.consensus.utils.DownloadDataUtils;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.handler.AbstractEventHandler;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class SmallBlockHandler extends AbstractEventHandler<SmallBlockEvent> {
    private BlockCacheManager blockCacheManager = BlockCacheManager.getInstance();
    private DownloadDataUtils downloadDataUtils = DownloadDataUtils.getInstance();

    @Override
    public void onEvent(SmallBlockEvent event, String fromId) {
        ValidateResult result = event.getEventBody().verify();
        if (result.isFailed()) {
            return;
        }
        blockCacheManager.cacheSmallBlock(event.getEventBody(), fromId);
        downloadDataUtils.removeSmallBlock(event.getEventBody().getBlockHash().getDigestHex());
    }
}
