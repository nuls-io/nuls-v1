package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.event.service.intf.EventService;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class EventServiceImpl implements EventService {
    private static EventServiceImpl INSTANCE = new EventServiceImpl();
    private EventServiceImpl(){}
    public static final EventServiceImpl getInstance(){
        return INSTANCE;
    }

    @Override
    public String broadcastWillPassNeedConfirmation(BaseNulsEvent event) {
        //todo
        return null;
    }

    @Override
    public String broadcastWillPass(BaseNulsEvent event) {
        //todo
        return null;
    }

    @Override
    public void broadcast(BaseNulsEvent event) {
        //todo
    }

    @Override
    public void sendToPeer(BaseNulsEvent event, String peerId) {
        //todo
    }
}
