package io.nuls.network.entity;

import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.db.entity.PeerPo;

/**
 * @author vivi
 * @date 2017/11/30.
 */
public class PeerTransfer {


    public static void transferToPeer(Peer peer, PeerPo po) {
        peer.setFailCount(po.getFailCount());
        peer.setIp(po.getIp());
        peer.setPort(po.getPort());
        peer.setLastTime(po.getLastTime());
        peer.setVersion(new NulsVersion(po.getVersion()));
    }


    public static PeerPo transferToPeerPo(Peer peer) {
        PeerPo po = new PeerPo();
        po.setFailCount(peer.getFailCount());
        po.setIp(peer.getIp());
        po.setPort(peer.getPort());
        po.setLastTime(peer.getLastTime());
        po.setMagicNum(peer.getNetwork().packetMagic());
        po.setVersion(peer.getVersion().getVersion());
        return po;
    }
}
