package io.nuls.network.message.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerTransfer;
import io.nuls.network.exception.NetworkMessageException;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataResult;
import io.nuls.network.message.entity.GetPeerData;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.messageHandler.NetWorkDataHandler;
import io.nuls.network.service.impl.ConnectionManager;
import io.nuls.network.service.impl.PeersManager;
import io.nuls.network.service.impl.TimeService;

import java.util.List;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class GetPeerDataHandler implements NetWorkDataHandler {

    private static final GetPeerDataHandler INSTANCE = new GetPeerDataHandler();

    private PeerDao peerDao;

    private ConnectionManager connectionManager;

    private PeersManager peersManager;

    private GetPeerDataHandler() {

    }

    public static GetPeerDataHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkDataResult process(BaseNetworkData message, Peer peer) {
        GetPeerData peerData = (GetPeerData) message;


//        List<Peer> list = peersManager.
//        peer.setVersionMessage(versionMessage);
//        peer.setStatus(Peer.HANDSHAKE);
//        peer.setLastTime(TimeService.currentTimeMillis());

//        getPeerDao().saveChange(PeerTransfer.transferToPeerPo(peer));
        return null;
    }

    private PeerDao getPeerDao() {
        if (peerDao == null) {
            while (NulsContext.getInstance().getService(PeerDao.class) == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            peerDao = NulsContext.getInstance().getService(PeerDao.class);
        }
        return peerDao;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setPeersManager(PeersManager peersManager) {
        this.peersManager = peersManager;
    }
}
