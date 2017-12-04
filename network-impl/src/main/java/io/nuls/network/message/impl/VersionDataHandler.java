package io.nuls.network.message.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.date.TimeService;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerTransfer;
import io.nuls.network.exception.NetworkMessageException;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataResult;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.messageHandler.NetWorkDataHandler;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class VersionDataHandler implements NetWorkDataHandler {

    private static final VersionDataHandler INSTANCE = new VersionDataHandler();

    private PeerDao peerDao;

    private VersionDataHandler() {

    }

    public static VersionDataHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkDataResult process(BaseNetworkData message, Peer peer) {
        VersionData versionMessage = (VersionData) message;
        if (versionMessage.getBestBlockHeight() < 0) {
            throw new NetworkMessageException(ErrorCode.NET_MESSAGE_ERROR);
        }
        peer.setVersionMessage(versionMessage);
        peer.setStatus(Peer.HANDSHAKE);
        peer.setLastTime(TimeService.currentTimeMillis());

        getPeerDao().saveChange(PeerTransfer.transferToPeerPo(peer));
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
}
