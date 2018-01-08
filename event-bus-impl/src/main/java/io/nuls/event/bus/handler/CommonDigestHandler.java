package io.nuls.event.bus.handler;

import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.event.GetEventBodyEvent;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.service.impl.EventBroadcasterImpl;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class CommonDigestHandler extends AbstractEventHandler<CommonDigestEvent> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private EventBroadcaster eventBroadcaster = EventBroadcasterImpl.getInstance();

    @Override
    public void onEvent(CommonDigestEvent event, String fromId) {
        boolean exist = eventCacheService.isKnown(event.getEventBody().getDigestHex());
        if (exist) {
            return;
        }
        GetEventBodyEvent getEventBodyEvent = new GetEventBodyEvent();
        getEventBodyEvent.setEventBody(event.getEventBody());
        eventBroadcaster.sendToNode(getEventBodyEvent, fromId);
    }
}
