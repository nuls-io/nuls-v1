package io.nuls.event.bus.handler;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.GetEventBodyEvent;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.service.impl.EventBroadcasterImpl;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class GetEventBodyHandler extends AbstractEventHandler<GetEventBodyEvent> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private EventBroadcaster eventBroadcaster = EventBroadcasterImpl.getInstance();

    @Override
    public void onEvent(GetEventBodyEvent event, String fromId)  {
        BaseEvent eventBody = eventCacheService.getEvent(event.getEventBody().getDigestHex());
        if (null == eventBody) {
            Log.warn("get event faild,node:" + fromId + ",event:" + event.getEventBody().getDigestHex());
            return;
        }
        eventBroadcaster.sendToNode(eventBody, fromId);
    }
}
