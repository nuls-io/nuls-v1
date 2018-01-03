package io.nuls.event.bus.handler;

import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.GetEventBodyEvent;
import io.nuls.event.bus.service.intf.NetworkEventBroadcaster;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.service.impl.NetworkEventBroadcasterImpl;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class GetEventBodyHandler extends AbstractNetworkEventHandler<GetEventBodyEvent> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private NetworkEventBroadcaster networkEventBroadcaster = NetworkEventBroadcasterImpl.getInstance();

    @Override
    public void onEvent(GetEventBodyEvent event, String fromId)  {
        BaseNetworkEvent eventBody = eventCacheService.getEvent(event.getEventBody().getDigestHex());
        if (null == eventBody) {
            Log.warn("get event faild,peer:" + fromId + ",event:" + event.getEventBody().getDigestHex());
            return;
        }
        networkEventBroadcaster.sendToPeer(eventBody, fromId);
    }
}
