package io.nuls.network.message.entity;

import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataHeader;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/12/1.
 */
public class GetPeerData extends BaseNetworkData {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private int length;


    public GetPeerData() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION, NetworkConstant.NETWORK_GET_PEER_MESSAGE);
    }

    public GetPeerData(int length) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION, NetworkConstant.NETWORK_GET_PEER_MESSAGE);
        this.length = length;
    }

    public GetPeerData(NulsByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public int size() {
        int s = 0;
        s += NetworkDataHeader.NETWORK_HEADER_SIZE;
        s += VarInt.sizeOf(length);
        return s;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        networkHeader.serializeToStream(stream);
        stream.write(new VarInt(length).encode());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        this.networkHeader = new NetworkDataHeader(byteBuffer);
        length = (int) byteBuffer.readVarInt();
    }


    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }
}
