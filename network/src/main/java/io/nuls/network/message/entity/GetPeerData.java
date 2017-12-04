package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataHeader;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @date 2017/12/1.
 */
public class GetPeerData extends BaseNetworkData {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private int size;

    public GetPeerData() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION, NetworkConstant.NETWORK_GET_PEER_MESSAGE);
    }

    public GetPeerData(int size) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION, NetworkConstant.NETWORK_GET_PEER_MESSAGE);
        this.size = size;
    }

    public GetPeerData(NulsByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public int size() {
        int s = 0;
        s += NetworkDataHeader.NETWORK_HEADER_SIZE;
        s += 2;    //version.length
        s += VarInt.sizeOf(size);
        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        networkHeader.serializeToStream(stream);
        Utils.int16ToByteStreamLE(version.getVersion(), stream);
        stream.write(new VarInt(size).encode());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        this.networkHeader = new NetworkDataHeader(byteBuffer);
        version = new NulsVersion(byteBuffer.readShort());
        size = (int) byteBuffer.readVarInt();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static void main(String[] args) throws IOException {
        GetPeerData data = new GetPeerData(40);
        byte[] bytes = data.serialize();
        data = new GetPeerData(new NulsByteBuffer(bytes));
        System.out.println(data.getSize());
    }
}
