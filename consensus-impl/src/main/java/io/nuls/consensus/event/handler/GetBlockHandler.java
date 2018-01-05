package io.nuls.consensus.event.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.handler.AbstractNetworkEventHandler;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class GetBlockHandler extends AbstractNetworkEventHandler<GetBlockEvent> {

    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);
    private NetworkEventBroadcaster networkEventBroadcaster = NulsContext.getInstance().getService(NetworkEventBroadcaster.class);
    @Override
    public void onEvent(GetBlockEvent event,String fromId)   {
        Block block = blockService.getBlock(event.getEventBody().getVal());
        BlockEvent blockEvent = new BlockEvent();
        blockEvent.setEventBody(block);
        networkEventBroadcaster.sendToNode(blockEvent,fromId);
    }
}
