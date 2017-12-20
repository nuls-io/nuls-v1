package io.nuls.event.bus.handler;

import io.nuls.core.exception.NulsException;
import io.nuls.event.bus.event.CommonHashEvent;
import io.nuls.event.bus.event.GetBodyEvent;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.service.impl.EventServiceImpl;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class EventHashHandler extends AbstractNetworkNulsEventHandler<CommonHashEvent> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private EventService eventService = EventServiceImpl.getInstance();

    @Override
    public void onEvent(CommonHashEvent event, String fromId) {
        boolean exist = eventCacheService.isKnown(event.getEventBody().getDigestHex());
        if (exist) {
            return;
        }
        GetBodyEvent getBodyEvent = new GetBodyEvent();
        getBodyEvent.setEventBody(event.getEventBody());
        eventService.sendToPeer(getBodyEvent, fromId);
    }
}
