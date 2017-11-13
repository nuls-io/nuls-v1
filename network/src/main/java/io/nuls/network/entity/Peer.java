package io.nuls.network.entity;

import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.ByteBuffer;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.message.entity.VersionMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Peer extends NulsData{

    private String hash;

    private String ip;

    private String hostName;

    private int port;

    /** 1: inPeer ,  2: outPeer */
    private int type;

    /** 0: wait , 1: connecting, 2: close*/
    private int status;


    public final static int WAIT = 0;
    public final static int CONNECTING = 1;
    public final static int CLOSE = 2;

    public Peer() {
    }

    public Peer(int type) {
        this.type = type;
    }


    public Peer(int type, InetSocketAddress socketAddress) {
        this.type = type;
        this.ip = socketAddress.getAddress().getHostAddress();
        this.hostName = socketAddress.getHostName();
        this.port = socketAddress.getPort();
    }

    public void connect(NetworkParam network) {
//        VersionMessage message = new VersionMessage();

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

    @Override
    public void verify() throws NulsException {

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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    public static void main(String[] args) {
        InetSocketAddress address = new InetSocketAddress("www.baidu.com",22);
        System.out.println(address.getHostName());
        System.out.println(address.getHostString());
        System.out.println(address.getAddress().getHostAddress());
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
