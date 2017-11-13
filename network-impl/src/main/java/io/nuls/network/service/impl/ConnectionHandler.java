package io.nuls.network.service.impl;

import io.nuls.core.chain.entity.Block;
import io.nuls.network.entity.Peer;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * Created by win10 on 2017/11/13.
 */
public class ConnectionHandler {

    private SelectionKey key;

    private Peer peer;

    private ByteBuffer readBuffer;

    public ConnectionHandler(Peer peer, SelectionKey key) {
        this.key = key;
        this.peer = peer;
        if(peer != null) {
            readBuffer = ByteBuffer.allocate(Block.MAX_SIZE);
        }
    }
}
