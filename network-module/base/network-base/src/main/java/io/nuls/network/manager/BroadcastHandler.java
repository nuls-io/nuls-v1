package io.nuls.network.manager;

import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.network.base.NetworkParam;
import io.nuls.network.entity.BroadcastResult;

@Component
public class BroadcastHandler {

    private NetworkParam networkParam = NetworkParam.getInstance();

    @Autowired
    private NodeManager nodeManager;

    public BroadcastResult broadcast(NulsMessage event, boolean asyn) {
        return null;
    }
}
