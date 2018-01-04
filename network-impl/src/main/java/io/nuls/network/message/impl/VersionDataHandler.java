package io.nuls.network.message.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.utils.date.TimeService;
import io.nuls.db.dao.PeerDao;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.PeerTransfer;
import io.nuls.network.exception.NetworkMessageException;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.handler.NetWorkEventHandler;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class VersionDataHandler implements NetWorkEventHandler {
    @Override
    public NetworkEventResult process(BaseNetworkEvent message, Peer peer) {
        return null;
    }

//    private static final VersionDataHandler INSTANCE = new VersionDataHandler();
//
//    private PeerDao peerDao;
//
//    private VersionDataHandler() {
//
//    }
//
//    public static VersionDataHandler getInstance() {
//        return INSTANCE;
//    }
//
//    @Override
//    public NetworkEventResult process(BaseNetworkData message, Peer peer) {
//        VersionData versionMessage = (VersionData) message;
//        if (versionMessage.getBestBlockHeight() < 0) {
//            throw new NetworkMessageException(ErrorCode.NET_MESSAGE_ERROR);
//        }
//        peer.setVersionMessage(versionMessage);
//        peer.setStatus(Peer.HANDSHAKE);
//        peer.setLastTime(TimeService.currentTimeMillis());
//
//        getPeerDao().saveChange(PeerTransfer.transferToPeerPo(peer));
//        return null;
//    }
//
//    private PeerDao getPeerDao() {
//        if (peerDao == null) {
//            peerDao = NulsContext.getInstance().getService(PeerDao.class);
//        }
//        return peerDao;
//    }
}
