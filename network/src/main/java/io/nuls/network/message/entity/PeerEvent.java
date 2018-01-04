package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.event.BaseNetworkEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017/12/5.
 */
public class PeerEvent extends BaseNetworkEvent {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;


    private List<Peer> peers;

    public PeerEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_PEER_MESSAGE);
        this.version = new NulsVersion(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
    }

    @Override
    public Object copy() {
        return null;
    }
}
