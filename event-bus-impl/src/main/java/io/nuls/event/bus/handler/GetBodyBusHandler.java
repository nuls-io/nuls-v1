package io.nuls.event.bus.handler;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.event.GetBodyEvent;
import io.nuls.event.bus.bus.handler.AbstractEventBusHandler;
import io.nuls.event.bus.bus.service.intf.BusDataService;
import io.nuls.event.bus.service.impl.EventCacheService;
import io.nuls.event.bus.service.impl.BusDataServiceImpl;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class GetBodyBusHandler extends AbstractEventBusHandler<GetBodyEvent> {

    private EventCacheService eventCacheService = EventCacheService.getInstance();
    private BusDataService busDataService = BusDataServiceImpl.getInstance();

    @Override
    public void onEvent(GetBodyEvent event, String fromId)  {
        BaseNulsEvent eventBody = eventCacheService.getEvent(event.getEventBody().getDigestHex());
        if (null == eventBody) {
            Log.warn("get event faild,peer:" + fromId + ",event:" + event.getEventBody().getDigestHex());
            return;
        }
        busDataService.sendToPeer(eventBody, fromId);
    }
}
