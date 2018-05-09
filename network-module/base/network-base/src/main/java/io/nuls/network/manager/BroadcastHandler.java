package io.nuls.network.manager;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.network.connection.netty.NioChannelMap;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.protocol.message.base.BaseMessage;

@Component
public class BroadcastHandler {

    private NetworkParam networkParam = NetworkParam.getInstance();

    @Autowired
    private NodeManager nodeManager;

    public BroadcastResult broadcast(BaseMessage msg, boolean asyn) {
        return null;
    }


    public BroadcastResult broadcastToNode(BaseMessage msg, Node node, boolean asyn) {
      return broadcastToNode(msg, node.getId(), asyn);
    }

    public BroadcastResult broadcastToNode(BaseMessage msg, String nodeId, boolean asyn) {
        Node sendNode = nodeManager.getNode(nodeId);
        if(sendNode == null) {
            return new BroadcastResult(false, "node not found");
        }
        return broadcast(msg, sendNode, asyn);
    }

    private BroadcastResult broadcast(BaseMessage message, Node node, boolean asyn) {
        if (!node.isAlive() && node.getChannelId() == null) {
            return new BroadcastResult(false, "node not found");
        }
        SocketChannel channel = NioChannelMap.get(node.getChannelId());
        if (channel == null) {
            return new BroadcastResult(false, "node not found");
        }
        try {
            ChannelFuture future = channel.writeAndFlush(Unpooled.wrappedBuffer(message.serialize()));
            if (!asyn) {
                future.await();
                boolean success = future.isSuccess();
                if (!success) {
                    return new BroadcastResult(false, "network send message failed");
                }
            }
        }catch (Exception e) {
            Log.error(e);
            return new BroadcastResult(false, "network send message failed");
        }
        return new BroadcastResult(true, "OK");
    }
}
