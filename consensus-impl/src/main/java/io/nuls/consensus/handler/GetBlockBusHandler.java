package io.nuls.consensus.handler;

import io.nuls.consensus.event.BlockEvent;
import io.nuls.consensus.event.GetBlockEvent;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.context.NulsContext;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.event.bus.bus.service.intf.BusDataService;

/**
 *
 * @author facjas
 * @date 2017/11/16
 */
public class GetBlockBusHandler extends AbstractEventBusHandler<GetBlockEvent> {

    private BlockService blockService = NulsContext.getInstance().getService(BlockService.class);
    private BusDataService busDataService = NulsContext.getInstance().getService(BusDataService.class);
    @Override
    public void onEvent(GetBlockEvent event,String fromId)   {
        Block block = blockService.getBlockByHeight(event.getEventBody().getVal());
        BlockEvent blockEvent = new BlockEvent();
        blockEvent.setEventBody(block);
        busDataService.sendToPeer(blockEvent,fromId);
    }
}
