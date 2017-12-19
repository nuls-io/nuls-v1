package io.nuls.consensus.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.RegisterAgentEvent;
import io.nuls.consensus.service.cache.BlockCacheService;
import io.nuls.consensus.service.cache.ConsensusCacheService;
import io.nuls.consensus.utils.DistributedBlockDownloadUtils;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.constant.SeverityLevelEnum;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.utils.log.Log;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.network.service.NetworkService;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class BlockEventHandler extends AbstractNetworkNulsEventHandler<BlockEvent> {

    private BlockCacheService blockCacheService = BlockCacheService.getInstance();
    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);

    @Override
    public void onEvent(BlockEvent event, String fromId) throws NulsException {
        Block block = event.getEventBody();
        if (DistributedBlockDownloadUtils.getInstance().recieveBlock(fromId, block)) {
            return;
        }

        ValidateResult result = block.verify();
        if (result.isFailed()) {
            if (result.getLevel() == SeverityLevelEnum.FLAGRANT) {
                this.networkService.removePeer(fromId);
            }
            return;
        }
        blockCacheService.cacheBlock(block);
    }
}
