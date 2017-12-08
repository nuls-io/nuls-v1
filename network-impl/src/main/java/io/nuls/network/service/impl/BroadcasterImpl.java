package io.nuls.network.service.impl;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.event.BaseNulsEvent;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Peer;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.BroadcastContext;
import io.nuls.network.service.Broadcaster;

import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author vivi
 * @date 2017/11/29.
 */
public class BroadcasterImpl implements Broadcaster {

    private PeersManager peersManager;

    private AbstractNetworkParam network;

    private static Random random = new Random();

    public BroadcasterImpl(PeersManager peersManager, AbstractNetworkParam network) {
        this.peersManager = peersManager;
        this.network = network;
    }

    private BroadcastResult broadcast(NulsMessage message, String exculdePeerId) {
        List<Peer> broadPeers = peersManager.getAvailablePeers();

        if (broadPeers.size() == 0) {
            return new BroadcastResult(false, "no peer can be broadcast");
        }

        int successCount = 0;
        for (Peer peer : broadPeers) {
            try {
                if (StringUtils.isNotBlank(exculdePeerId) && peer.getHash().equals(exculdePeerId)) {
                    continue;
                }
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

    /**
     * Broadcast half of the peers, waiting for the other half to reply
     *
     * @param message
     * @return
     */
    private BroadcastResult broadcastSync(NulsMessage message, String exculdePeerId) {
        List<Peer> broadPeers = peersManager.getAvailablePeers();

        if (broadPeers.size() == 0) {
            return new BroadcastResult(false, "no peer can be broadcast");
        }
        int numConnected = broadPeers.size();
        int numToBroadcastTo = (int) Math.max(1, Math.round(Math.ceil(broadPeers.size() / 2.0)));
        Collections.shuffle(broadPeers, random);
        broadPeers = broadPeers.subList(0, numToBroadcastTo);


        int successCount = 0;
        for (Peer peer : broadPeers) {
            try {
                if (StringUtils.isNotBlank(exculdePeerId) && peer.getHash().equals(exculdePeerId)) {
                    continue;
                }
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
        BroadcastContext.get().add(result.getHash(), result);
        return result;
    }

    private BroadcastResult broadcastToGroup(NulsMessage message, String groupName, String exculdePeerId) {
        List<Peer> broadPeers = peersManager.getGroupAvailablePeers(groupName);
        if (broadPeers.size() == 0) {
            return new BroadcastResult(false, "no peer can be broadcast");
        }

        int successCount = 0;
        for (Peer peer : broadPeers) {
            try {
                if (StringUtils.isNotBlank(exculdePeerId) && peer.getHash().equals(exculdePeerId)) {
                    continue;
                }
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
        return broadcast(event, null);
    }

    @Override
    public BroadcastResult broadcast(BaseNulsEvent event, String exculdePeerId) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }

        return broadcast(message, exculdePeerId);
    }

    @Override
    public BroadcastResult broadcast(byte[] data) {
        return broadcast(data, null);
    }

    @Override
    public BroadcastResult broadcast(byte[] data, String exculdePeerId) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcast(message, exculdePeerId);
    }

    @Override
    public BroadcastResult broadcastSync(BaseNulsEvent event) {
        return broadcastSync(event, null);
    }

    @Override
    public BroadcastResult broadcastSync(BaseNulsEvent event, String exculdePeerId) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }

        return broadcastSync(message, exculdePeerId);
    }

    @Override
    public BroadcastResult broadcastSync(byte[] data) {
        return broadcastSync(data, null);
    }

    @Override
    public BroadcastResult broadcastSync(byte[] data, String exculdePeerId) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcastSync(message, exculdePeerId);
    }

    @Override
    public BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName) {
        return broadcastToGroup(event, groupName, null);
    }

    @Override
    public BroadcastResult broadcastToGroup(BaseNulsEvent event, String groupName, String exculdePeerId) {
        NulsMessage message = null;
        try {
            message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, event.serialize());
        } catch (IOException e) {
            return new BroadcastResult(false, "event.serialize() error");
        }
        return broadcastToGroup(message, groupName, exculdePeerId);
    }

    @Override
    public BroadcastResult broadcastToGroup(byte[] data, String groupName) {
        return broadcastToGroup(data, groupName, null);
    }

    @Override
    public BroadcastResult broadcastToGroup(byte[] data, String groupName, String exculdePeerId) {
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.EVENT_MESSAGE, data);
        return broadcastToGroup(message, groupName, exculdePeerId);
    }

}
