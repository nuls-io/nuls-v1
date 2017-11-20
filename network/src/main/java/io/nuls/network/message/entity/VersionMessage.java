package io.nuls.network.message.entity;

import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.message.NetworkMessage;
import io.nuls.network.message.NetworkMessageHeader;

/**
 * Created by win10 on 2017/11/9.
 */
public class VersionMessage extends NetworkMessage {


    private long bestBlockHeight;

    private String bestBlockHash;

    private int port;

    private String ip;

    public VersionMessage(NetworkParam network, long height, String hash, Peer peer) {

        super(network);
        this.bestBlockHash = hash;
        this.bestBlockHeight = height;
        this.port = peer.getPort();
        this.ip = peer.getIp();
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
