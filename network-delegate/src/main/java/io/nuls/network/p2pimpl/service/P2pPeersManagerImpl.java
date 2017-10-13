package io.nuls.network.p2pimpl.service;

import io.nuls.global.NulsContext;
import io.nuls.network.intf.IPeersManager;

public class P2pPeersManagerImpl implements IPeersManager {

    private P2pPeersManagerImpl() {
    }

    private static final P2pPeersManagerImpl service = new P2pPeersManagerImpl();

    public static P2pPeersManagerImpl getInstance() {
        return service;
    }
    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void restart() {

    }

    @Override
    public String info() {
        return "";
    }
}
