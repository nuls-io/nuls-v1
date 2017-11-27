package io.nuls.network.message.entity;

import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.AbstractNetworkMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class VersionMessage extends AbstractNetworkMessage {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private long bestBlockHeight;

    private String bestBlockHash;

    private String nulsVersion;

    public VersionMessage() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.type = NetworkConstant.NETWORK_VERSION_MESSAGE;
        this.nulsVersion = NulsContext.nulsVersion;
    }

    public VersionMessage(long bestBlockHeight, String bestBlockHash) {
        this();
        this.bestBlockHash = bestBlockHash;
        this.bestBlockHeight = bestBlockHeight;
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(version.getVersion());
        s += VarInt.sizeOf(type);
        s += VarInt.sizeOf(bestBlockHeight);
        // put the bestBlockHash.length
        s += 1;
        if (!StringUtils.isBlank(bestBlockHash)) {
            try {
                s += bestBlockHash.getBytes(NulsContext.DEFAULT_ENCODING).length;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        }
        s += 1;
        if (!StringUtils.isBlank(nulsVersion)) {
            try {
                s += nulsVersion.getBytes(NulsContext.DEFAULT_ENCODING).length;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        }
        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(type).encode());
        stream.write(new VarInt(version.getVersion()).encode());
        stream.write(new VarInt(bestBlockHeight).encode());
        this.writeBytesWithLength(stream, bestBlockHash);
        this.writeBytesWithLength(stream, nulsVersion);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        type = (short) byteBuffer.readVarInt();
        version = new NulsVersion((short) byteBuffer.readVarInt());
        bestBlockHeight = byteBuffer.readVarInt();
        bestBlockHash = new String(byteBuffer.readByLengthByte());
        nulsVersion = new String(byteBuffer.readByLengthByte());
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("versionMessage:{");
        buffer.append("type:" + type + ", ");
        buffer.append("version:" + version.getStringVersion() + ", ");
        buffer.append("bestBlockHeight:" + bestBlockHeight + ", ");
        buffer.append("bestBlockHash:" + bestBlockHash + ", ");
        buffer.append("nulsVersion:" + nulsVersion + "}");

        return buffer.toString();
    }

    public long getBestBlockHeight() {
        return bestBlockHeight;
    }

    public void setBestBlockHeight(long bestBlockHeight) {
        this.bestBlockHeight = bestBlockHeight;
    }

    public String getBestBlockHash() {
        return bestBlockHash;
    }

    public void setBestBlockHash(String bestBlockHash) {
        this.bestBlockHash = bestBlockHash;
    }


    public String getNulsVersion() {
        return nulsVersion;
    }

    public void setNulsVersion(String nulsVersion) {
        this.nulsVersion = nulsVersion;
    }


}
