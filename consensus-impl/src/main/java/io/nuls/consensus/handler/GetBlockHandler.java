package io.nuls.consensus.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.event.handler.AbstractEventHandler;
import io.nuls.event.bus.event.service.intf.EventService;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class GetBlockHandler extends AbstractEventHandler<GetBlockEvent> {

    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);
    private EventService eventService = NulsContext.getInstance().getService(EventService.class);
    @Override
    public void onEvent(GetBlockEvent event,String fromId)   {
        Block block = blockService.getBlockByHeight(event.getEventBody().getVal());
        BlockEvent blockEvent = new BlockEvent();
        blockEvent.setEventBody(block);
        eventService.sendToPeer(blockEvent,fromId);
    }
}
