package io.nuls.consensus.handler;

import io.nuls.consensus.event.BlockHeaderEvent;
import io.nuls.consensus.event.GetSmallBlockEvent;
import io.nuls.consensus.service.cache.BlockHeaderCacheService;
import io.nuls.consensus.utils.DistributedBlockInfoRequestUtils;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.chain.entity.BlockHeader;
import io.nuls.core.context.NulsContext;
import io.nuls.core.validate.ValidateResult;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.network.service.NetworkService;

/**
 * @author facjas
 * @date 2017/11/16
 */
public class BlockHeaderHandler extends AbstractNetworkEventHandler<BlockHeaderEvent> {

    private BlockHeaderCacheService headerCacheService = BlockHeaderCacheService.getInstance();

    private NetworkService networkService = NulsContext.getInstance().getService(NetworkService.class);
    private NetworkEventBroadcaster networkEventBroadcaster = NulsContext.getInstance().getService(NetworkEventBroadcaster.class);

    @Override
    public void onEvent(BlockHeaderEvent event, String fromId) {
        if (DistributedBlockInfoRequestUtils.getInstance().addBlockHeader(fromId, event.getEventBody())) {
            return;
        }
        BlockHeader header = event.getEventBody();
        //todo 分叉处理
        ValidateResult result = header.verify();
        if (result.isFailed()) {
            networkService.removePeer(fromId);
            return;
        }
        headerCacheService.cacheHeader(header);
        GetSmallBlockEvent getSmallBlockEvent = new GetSmallBlockEvent();
        BasicTypeData<Long> data = new BasicTypeData<>(header.getHeight());
        getSmallBlockEvent.setEventBody(data);
        networkEventBroadcaster.sendToPeer(getSmallBlockEvent, fromId);
    }
}
