package io.nuls.network.entity;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.message.entity.VersionMessage;
import io.nuls.network.service.MessageWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Peer extends NulsData {

    private String hash;

    private String ip;

    private int port;

    /**
     * 1: inPeer ,  2: outPeer
     */
    public final static int IN = 1;
    public final static int OUT = 2;
    private int type;

    /**
     * 0: wait , 1: connecting, 2: handshake 3: close
     */
    public final static int WAIT = 0;
    public final static int CONNECTING = 1;
    public final static int HANDSHAKE = 2;
    public final static int CLOSE = 3;
    private int status;

    private MessageWriter writeTarget;

    private VersionMessage versionMessage;


    public Peer() {
    }

    public Peer(int type) {
        this.type = type;
    }


    public Peer(int type, InetSocketAddress socketAddress) {
        this.type = type;
        this.port = socketAddress.getPort();
        this.ip = socketAddress.getAddress().getHostAddress();
    }

    public void connect(NetworkParam network) {
//

    }

    public void connectionOpened() {
        Block bestBlock = NulsContext.getInstance().getBestBlock();
        VersionMessage message = new VersionMessage(bestBlock.getHeight(), bestBlock.getHash().toString(), this);

    }


    public void sendMessage() {

    }

    public void destroy() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(ByteBuffer byteBuffer) {

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress("www.baidu.com", 22);
        Log.debug(address.getHostName());
        Log.debug(address.getHostString());
        Log.debug(address.getAddress().getHostAddress());
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MessageWriter getWriteTarget() {
        return writeTarget;
    }

    public void setWriteTarget(MessageWriter writeTarget) {
        this.writeTarget = writeTarget;
    }

    public VersionMessage getVersionMessage() {
        return versionMessage;
    }

    public void setVersionMessage(VersionMessage versionMessage) {
        this.versionMessage = versionMessage;
    }
}
