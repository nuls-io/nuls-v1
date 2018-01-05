package io.nuls.event.bus.handler;

import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.event.GetEventBodyEvent;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.service.impl.NetworkEventBroadcasterImpl;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class CommonDigestHandler extends AbstractNetworkEventHandler<CommonDigestEvent> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private NetworkEventBroadcaster networkEventBroadcaster = NetworkEventBroadcasterImpl.getInstance();

    @Override
    public void onEvent(CommonDigestEvent event, String fromId) {
        boolean exist = eventCacheService.isKnown(event.getEventBody().getDigestHex());
        if (exist) {
            return;
        }
        GetEventBodyEvent getEventBodyEvent = new GetEventBodyEvent();
        getEventBodyEvent.setEventBody(event.getEventBody());
        networkEventBroadcaster.sendToNode(getEventBodyEvent, fromId);
    }
}
