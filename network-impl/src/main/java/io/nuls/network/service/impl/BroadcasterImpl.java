package io.nuls.network.service.impl;

import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.service.Broadcaster;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.List;

/**
 * @author vivi
 * @date 2017/11/29.
 */
public class BroadcasterImpl implements Broadcaster {

    private PeersManager peersManager;

    private AbstractNetworkParam network;

    public BroadcasterImpl(PeersManager peersManager, AbstractNetworkParam network) {
        this.peersManager = peersManager;
        this.network = network;
    }

    private BroadcastResult broadcast(NulsMessage message) {
        List<Peer> broadPeers = peersManager.getAvailablePeers();

        if (broadPeers.size() == 0) {
            return new BroadcastResult(false, "no peer can be broadcast");
        }

        int successCount = 0;
        for (Peer peer : broadPeers) {
            try {
                peer.sendMessage(message);
                successCount++;
            } catch (NotYetConnectedException | IOException e) {
                Log.warn("broadcast message error ， maybe the peer closed ! peer ip :{}, {}", peer.getIp(), e.getMessage());
            }
        }

        if (successCount == 0) {
            new BroadcastResult(false, "broadcast fail");
        }
        Log.debug("成功广播给{}个节点，消息{}", successCount, message);
        return new BroadcastResult(true, "OK");
    }

    private BroadcastResult broadcastToGroup(NulsMessage message, String groupName) {
        if (!peersManager.hasPeerGroup(groupName)) {
            return new BroadcastResult(false, "There is no such group");
        }

        List<Peer> broadPeers = peersManager.getAvailablePeers();
        if (broadPeers.size() == 0) {
            return new BroadcastResult(false, "no peer can be broadcast");
        }

        int successCount = 0;
        for (Peer peer : broadPeers) {
            try {
                peer.sendMessage(message);
                successCount++;
            } catch (NotYetConnectedException | IOException e) {
                Log.warn("broadcast message error ， maybe the peer closed ! peer ip :{}, {}", peer.getIp(), e.getMessage());
            }
        }

        if (successCount == 0) {
            new BroadcastResult(false, "broadcast fail");
        }
        Log.debug("成功广播给{}个节点，消息{}", successCount, message);
        return new BroadcastResult(true, "OK");
    }

    @Override
    public BroadcastResult broadcast(BaseNulsEvent event) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }

        return broadcast(message);
    }

    @Override
    public BroadcastResult broadcast(byte[] data) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcast(message);
    }

    @Override
    public BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }
        return broadcastToGroup(message, groupName);
    }

    @Override
    public BroadcastResult broadcastToGroup(byte[] data, String groupName) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcastToGroup(message, groupName);
    }

}
