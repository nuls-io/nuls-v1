package io.nuls.network.message.entity;

import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Peer;
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

    public static final short OWN_SUB_VERSION = 0001;

    private long bestBlockHeight;

    private String bestBlockHash;

    private int port;

    private String ip;

    public VersionMessage() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.type = NetworkConstant.NETWORK_VERSION_MESSAGE;
    }

    public VersionMessage(long height, String hash, Peer peer) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.type = NetworkConstant.NETWORK_VERSION_MESSAGE;
        this.bestBlockHash = hash;
        this.bestBlockHeight = height;
        this.port = peer.getPort();
        this.ip = peer.getIp();
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(version);
        s += VarInt.sizeOf(bestBlockHeight);
        // put the bestBlockHash.length
        s += 1;
        if (StringUtils.isBlank(bestBlockHash)) {
            try {
                s += bestBlockHash.getBytes(NulsContext.DEFAULT_ENCODING).length;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        }
        s += VarInt.sizeOf(port);
        // the ip.length
        s += 1;
        if (StringUtils.isBlank(ip)) {
            try {
                s += ip.getBytes(NulsContext.DEFAULT_ENCODING).length;
            } catch (UnsupportedEncodingException e) {
                Log.error(e);
            }
        }
        return s;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {
        stream.write(new VarInt(bestBlockHeight).encode());
        this.writeBytesWithLength(stream, bestBlockHash);
        stream.write(new VarInt(port).encode());
        this.writeBytesWithLength(stream, ip);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) {
        bestBlockHeight = (int) byteBuffer.readUint32();
        bestBlockHash = new String(byteBuffer.readByLengthByte());
        port = (int) byteBuffer.readUint32();
        ip = new String(byteBuffer.readByLengthByte());
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

}
