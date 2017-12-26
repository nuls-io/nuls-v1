package io.nuls.event.bus.handler;

import io.nuls.event.bus.event.CommonHashEvent;
import io.nuls.event.bus.event.GetBodyEvent;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.event.bus.bus.service.intf.EventBroadcaster;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.service.impl.EventBroadcasterImpl;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class HashBusHandler extends AbstractEventBusHandler<CommonHashEvent> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private EventBroadcaster eventBroadcaster = EventBroadcasterImpl.getInstance();

    @Override
    public void onEvent(CommonHashEvent event, String fromId) {
        boolean exist = eventCacheService.isKnown(event.getEventBody().getDigestHex());
        if (exist) {
            return;
        }
        GetBodyEvent getBodyEvent = new GetBodyEvent();
        getBodyEvent.setEventBody(event.getEventBody());
        eventBroadcaster.sendToPeer(getBodyEvent, fromId);
    }
}
