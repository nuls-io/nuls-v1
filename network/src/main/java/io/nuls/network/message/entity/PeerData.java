package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017/12/5.
 */
public class PeerData extends BaseNetworkData {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private List<Peer> peers;

    public PeerData() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION, NetworkConstant.NETWORK_PEER_MESSAGE);
    }

    public PeerData(NulsByteBuffer buffer) throws NulsException {
        super(buffer);
    }

    @Override
    public int size() {
        int s = 0;
        s += NetworkDataHeader.NETWORK_HEADER_SIZE;

        s += 2;    // version.length
        s += 1;    // peers.size
        for (int i = 0; i < peers.size(); i++) {
            s += peers.get(i).size();
        }

        return s;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        networkHeader.serializeToStream(stream);
        stream.writeShort(version.getVersion());
        stream.write(new VarInt(peers.size()).encode());
        for (Peer peer : peers) {
            peer.serializeToStream(stream);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        this.networkHeader = new NetworkDataHeader(byteBuffer);
        version = new NulsVersion(byteBuffer.readShort());
        int size = (int) byteBuffer.readVarInt();
        List<Peer> peers = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Peer peer = new Peer(byteBuffer);
            peers.add(peer);
        }
        this.peers = peers;
    }


    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

}
