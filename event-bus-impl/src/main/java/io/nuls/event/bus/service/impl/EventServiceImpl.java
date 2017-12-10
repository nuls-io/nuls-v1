package io.nuls.event.bus.service.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.event.bus.event.service.intf.EventService;
import io.nuls.network.service.NetworkService;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class EventServiceImpl implements EventService {
    private static EventServiceImpl INSTANCE = new EventServiceImpl();

    private NetworkService networkService;

    private EventServiceImpl() {
    }

    public static final EventServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public String broadcastWillPassNeedConfirmation(BaseNulsEvent event) {
        initNetworkService();
        //todo
        return null;
    }

    @Override
    public String broadcastWillPass(BaseNulsEvent event) {
        initNetworkService();
        //todo
        return null;
    }

    @Override
    public void broadcast(BaseNulsEvent event) {
        initNetworkService();
        //todo
    }

    @Override
    public void sendToPeer(BaseNulsEvent event, String peerId) {
        initNetworkService();
        //todo
    }

    private void initNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getInstance().getService(NetworkService.class);
        }
    }
}
