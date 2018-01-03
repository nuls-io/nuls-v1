package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataHeader;

import java.io.IOException;

/**
 * @author vivi
 * @date 2017/11/24.
 */
public class GetVersionData extends BaseNetworkData {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private int externalPort;

    public GetVersionData(int externalPort) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION, NetworkConstant.NETWORK_GET_VERSION_MESSAGE);
        this.externalPort = externalPort;
    }

    public GetVersionData(NulsByteBuffer buffer) throws NulsException {
        super(buffer);
    }


    @Override
    public int size() {
        int s = 0;
        s += NetworkDataHeader.NETWORK_HEADER_SIZE;
        s += 2;    //version.length
        s += VarInt.sizeOf(externalPort);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        networkHeader.serializeToStream(stream);
        stream.writeShort(version.getVersion());
        stream.write(new VarInt(externalPort).encode());
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.networkHeader = new NetworkDataHeader(byteBuffer);
        version = new NulsVersion(byteBuffer.readShort());
        externalPort = (int) byteBuffer.readVarInt();
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("getVersionData:{");
        buffer.append(networkHeader.toString() + ", ");
        buffer.append("version:" + version.getVersion() + ", ");
        buffer.append("externalPort:" + externalPort + "}");

        return buffer.toString();
    }

    public int getExternalPort() {
        return externalPort;
    }

    public void setExternalPort(int externalPort) {
        this.externalPort = externalPort;
    }
}
