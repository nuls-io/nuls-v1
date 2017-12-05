package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataHeader;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionData extends BaseNetworkData {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private long nonce;

    public GetVersionData() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION, NetworkConstant.NETWORK_GET_VERSION_MESSAGE);
        nonce = Utils.randomLong();
    }

    public GetVersionData(NulsByteBuffer buffer) {
        super(buffer);
    }


    @Override
    public int size() {
        int s = 0;
        s += NetworkDataHeader.NETWORK_HEADER_SIZE;
        s += 2;    //version.length
        s += VarInt.sizeOf(nonce);
        return s;
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        networkHeader.serializeToStream(stream);
        stream.writeShort(version.getVersion());
        stream.write(new VarInt(nonce).encode());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        this.networkHeader = new NetworkDataHeader(byteBuffer);
        version = new NulsVersion(byteBuffer.readShort());
        nonce = byteBuffer.readVarInt();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("getVersionData:{");
        buffer.append(networkHeader.toString() + ", ");
        buffer.append("version:" + version.getVersion() + ", ");
        buffer.append("nonce:" + nonce + "}");

        return buffer.toString();
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

}
