package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.entity.Peer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017/12/5.
 */
public class PeerEventBody extends BaseNulsData {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private List<Peer> peers;

    public PeerEventBody() {
        this.peers = new ArrayList<>();
    }

    public PeerEventBody(List peers) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.peers = peers;
    }

    @Override
    public int size() {
        int s = 2;   //version size;
        s += VarInt.sizeOf(peers.size());
        for (Peer peer : peers) {
            s += peer.size();
        }
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(version.getVersion());
        stream.write(new VarInt(peers.size()).encode());
        for (Peer peer : peers) {
            stream.writeNulsData(peer);
        }
    }

    @Override
    protected void parse(NulsByteBuffer buffer) throws NulsException {
        version = new NulsVersion(buffer.readShort());
        int size = (int) buffer.readVarInt();
        for (int i = 0; i < size; i++) {
            peers.add(buffer.readNulsData(new Peer()));
        }
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }
}
