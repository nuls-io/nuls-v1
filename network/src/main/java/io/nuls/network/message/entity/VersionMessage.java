package io.nuls.network.message.entity;

import io.nuls.network.entity.Peer;
import io.nuls.network.message.NetworkMessageHeader;

/**
 * Created by win10 on 2017/11/9.
 */
public class VersionMessage {

    private NetworkMessageHeader header;

    private int version;

    private long bestBlockHeight;

    private String bestBlockHash;

    private int port;

    private String ip;

    public VersionMessage(long height, String hash, Peer peer) {
        this.bestBlockHash = hash;
        this.bestBlockHeight = height;
        this.port = peer.getPort();
        this.ip = peer.getIp();
    }
}
