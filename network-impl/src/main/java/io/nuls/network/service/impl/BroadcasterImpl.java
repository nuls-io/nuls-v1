package io.nuls.network.service.impl;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.NetworkCacheService;
import io.nuls.network.service.Broadcaster;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.Collections;
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

    private BroadcastResult broadcast(NulsMessage message, String excludePeerId) {
        List<Peer> broadPeers = peersManager.getAvailablePeers(excludePeerId);
        //only one peer connected can't send message
        if (broadPeers.size() <= 1) {
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
            return new BroadcastResult(false, "broadcast fail");
        }
        Log.debug("成功广播给{}个节点，消息{}", successCount, message);
        return new BroadcastResult(true, "OK");
    }

    /**
     * Broadcast half of the peers, waiting for the other half to reply
     *
     * @param message
     * @return
     */
    private BroadcastResult broadcastSync(NulsMessage message, String excludePeerId) {
        List<Peer> broadPeers = peersManager.getAvailablePeers(excludePeerId);

        if (broadPeers.size() <= 1) {
            return new BroadcastResult(false, "no peer can be broadcast");
        }

        int numConnected = broadPeers.size();
        int numToBroadcastTo = (int) Math.max(1, Math.round(Math.ceil(broadPeers.size() / 2.0)));
        Collections.shuffle(broadPeers);
        broadPeers = broadPeers.subList(0, numToBroadcastTo);

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
            return new BroadcastResult(false, "broadcast fail");
        }

        BroadcastResult result = new BroadcastResult(true, "OK");
        result.setHash(Sha256Hash.twiceOf(message.getData()).toString());
        result.setBroadcastPeers(broadPeers);
        result.setWaitReplyCount(numConnected - numToBroadcastTo);
        NetworkCacheService.getInstance().addBroadCastResult(result);

        return result;
    }


    private BroadcastResult broadcastToPeer(NulsMessage message, String peerId) {
        Peer peer = peersManager.getPeer(peerId);
        if (peer == null || peer.getStatus() != Peer.HANDSHAKE) {
            return new BroadcastResult(false, "no peer can be broadcast");
        }
        try {
            peer.sendMessage(message);
        } catch (NotYetConnectedException | IOException e) {
            Log.warn("broadcast message error ， maybe the peer closed ! peer ip :{}, {}", peer.getIp(), e.getMessage());
            return new BroadcastResult(false, "broadcast fail");
        }
        return new BroadcastResult(true, "OK");
    }

    private BroadcastResult broadcastToGroup(NulsMessage message, String groupName, String excludePeerId) {
        List<Peer> broadPeers = peersManager.getGroupAvailablePeers(groupName, excludePeerId);
        if (broadPeers.size() <= 1) {
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

    private BroadcastResult broadcastToGroupSync(NulsMessage message, String groupName, String excludePeerId) {
        List<Peer> broadPeers = peersManager.getGroupAvailablePeers(groupName, excludePeerId);
        if (broadPeers.size() <= 1) {
            return new BroadcastResult(false, "no peer can be broadcast");
        }

        int numConnected = broadPeers.size();
        int numToBroadcastTo = (int) Math.max(1, Math.round(Math.ceil(broadPeers.size() / 2.0)));
        Collections.shuffle(broadPeers);
        broadPeers = broadPeers.subList(0, numToBroadcastTo);

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

        BroadcastResult result = new BroadcastResult(true, "OK");
        result.setHash(Sha256Hash.twiceOf(message.getData()).toString());
        result.setBroadcastPeers(broadPeers);
        result.setWaitReplyCount(numConnected - numToBroadcastTo);
        NetworkCacheService.getInstance().addBroadCastResult(result);
        return result;
    }


    @Override
    public BroadcastResult broadcast(BaseNulsEvent event) {
        return broadcast(event, null);
    }

    @Override
    public BroadcastResult broadcast(BaseNulsEvent event, String excludePeerId) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }

        return broadcast(message, excludePeerId);
    }

    @Override
    public BroadcastResult broadcast(byte[] data) {
        return broadcast(data, null);
    }

    @Override
    public BroadcastResult broadcast(byte[] data, String excludePeerId) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcast(message, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastSync(BaseNulsEvent event) {
        return broadcastSync(event, null);
    }

    @Override
    public BroadcastResult broadcastSync(BaseNulsEvent event, String excludePeerId) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }

        return broadcastSync(message, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastSync(byte[] data) {
        return broadcastSync(data, null);
    }

    @Override
    public BroadcastResult broadcastSync(byte[] data, String excludePeerId) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcastSync(message, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastToPeer(BaseNulsEvent event, String peerId) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }
        return broadcastToPeer(message, peerId);
    }

    @Override
    public BroadcastResult broadcastToPeer(byte[] data, String peerId) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcastToPeer(message, peerId);
    }

    @Override
    public BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName) {
        return broadcastToGroup(event, groupName, null);
    }

    @Override
    public BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName, String excludePeerId) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }
        return broadcastToGroup(message, groupName, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastToGroup(byte[] data, String groupName) {
        return broadcastToGroup(data, groupName, null);
    }

    @Override
    public BroadcastResult broadcastToGroup(byte[] data, String groupName, String excludePeerId) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcastToGroup(message, groupName, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastToGroupSync(BaseNulsEvent event, String groupName) {
        return broadcastToGroupSync(event, groupName, null);
    }

    @Override
    public BroadcastResult broadcastToGroupSync(BaseNulsEvent event, String groupName, String excludePeerId) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }
        return broadcastToGroupSync(message, groupName, excludePeerId);
    }

    @Override
    public BroadcastResult broadcastToGroupSync(byte[] data, String groupName) {
        return broadcastToGroupSync(data, groupName, null);
    }

    @Override
    public BroadcastResult broadcastToGroupSync(byte[] data, String groupName, String excludePeerId) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcastToGroupSync(message, groupName, excludePeerId);
    }

}
