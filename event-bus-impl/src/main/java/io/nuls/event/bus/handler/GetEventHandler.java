package io.nuls.event.bus.handler;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.GetBodyEvent;
import io.nuls.event.bus.event.handler.AbstractNetworkNulsEventHandler;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.service.impl.EventServiceImpl;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class GetEventHandler extends AbstractNetworkNulsEventHandler<GetBodyEvent> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private EventService eventService = EventServiceImpl.getInstance();

    @Override
    public void onEvent(GetBodyEvent event, String fromId) throws NulsException {
        BaseNulsEvent eventBody = eventCacheService.getEvent(event.getEventBody().getDigestHex());
        if (null == eventBody) {
            Log.warn("get event faild,peer:" + fromId + ",event:" + event.getEventBody().getDigestHex());
            return;
        }
        eventService.sendToPeer(eventBody, fromId);
    }
}
