package io.nuls.consensus.handler;

import io.nuls.consensus.event.BestBlockEvent;
import io.nuls.consensus.thread.BlockMaintenanceThread;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.event.bus.event.service.intf.EventService;

/**
 * @author Niels
 * @date 2017/12/11
 */
public class BestBlockHandler extends AbstractNetworkNulsEventHandler<BestBlockEvent> {
    private final EventService eventService = NulsContext.getInstance().getService(EventService.class);

    @Override
    public void onEvent(BestBlockEvent event, String fromId) throws NulsException {
        BlockMaintenanceThread.BEST_HEIGHT_FROM_NET.addBlockHeader(fromId,event.getEventBody());
    }
}
