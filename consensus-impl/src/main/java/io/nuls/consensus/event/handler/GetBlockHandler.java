package io.nuls.consensus.event.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.GetBlockRequest;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.handler.AbstractEventHandler;
import io.nuls.event.bus.service.intf.EventBroadcaster;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class GetBlockHandler extends AbstractEventHandler<GetBlockRequest> {

    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);
    private EventBroadcaster eventBroadcaster = NulsContext.getInstance().getService(EventBroadcaster.class);
    @Override
    public void onEvent(GetBlockRequest event, String fromId)   {
        Block block = blockService.getBlock(event.getEventBody().getVal());
        BlockEvent blockEvent = new BlockEvent();
        blockEvent.setEventBody(block);
        eventBroadcaster.sendToNode(blockEvent,fromId);
    }
}
