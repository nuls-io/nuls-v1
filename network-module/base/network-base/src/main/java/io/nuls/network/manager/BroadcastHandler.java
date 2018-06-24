/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.network.manager;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.SocketChannel;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.network.connection.netty.NioChannelMap;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.BroadcastResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BroadcastHandler {

    private static BroadcastHandler instance = new BroadcastHandler();

    private BroadcastHandler() {

    }

    public static BroadcastHandler getInstance() {
        return instance;
    }

    private NetworkParam networkParam = NetworkParam.getInstance();

    private NodeManager nodeManager = NodeManager.getInstance();

    public BroadcastResult broadcastToAllNode(BaseMessage msg, Node excludeNode, boolean asyn) {
        if (nodeManager.getAvailableNodes().isEmpty()) {
            return new BroadcastResult(false, NetworkErrorCode.NET_BROADCAST_NODE_EMPTY);
        }
        return broadcastToList(nodeManager.getAvailableNodes(), msg, excludeNode, asyn);
    }

    public BroadcastResult broadcastToHalfNode(BaseMessage msg, Node excludeNode, boolean asyn) {

        if (nodeManager.getAvailableNodes().isEmpty()) {
            return new BroadcastResult(false, NetworkErrorCode.NET_BROADCAST_NODE_EMPTY);
        }
        List<Node> nodeList = new ArrayList<>();
        int i = 0;
        for (Node node : nodeManager.getAvailableNodes()) {
            i++;
            if (i % 2 == 1) {
                nodeList.add(node);
            }
        }

        return broadcastToList(nodeList, msg, excludeNode, asyn);
    }

    public BroadcastResult broadcastToNode(BaseMessage msg, Node sendNode, boolean asyn) {
        if (sendNode == null) {
            return new BroadcastResult(false, NetworkErrorCode.NET_NODE_NOT_FOUND);
        }
        return broadcastToANode(msg, sendNode, asyn);
    }

    public BroadcastResult broadcastToNodeGroup(BaseMessage msg, String groupName, boolean asyn) {
        NodeGroup group = nodeManager.getNodeGroup(groupName);
        if (group == null || group.size() == 0) {
            return new BroadcastResult(false, NetworkErrorCode.NET_BROADCAST_NODE_EMPTY);
        }
        return broadcastToList(group.getNodes().values(), msg, null, asyn);
    }

    public BroadcastResult broadcastToNodeGroup(BaseMessage msg, String groupName, Node excludeNode, boolean asyn) {
        NodeGroup group = nodeManager.getNodeGroup(groupName);
        if (group == null || group.size() == 0) {
            return new BroadcastResult(false, NetworkErrorCode.NET_BROADCAST_NODE_EMPTY);
        }
        return broadcastToList(group.getNodes().values(), msg, excludeNode, asyn);
    }

    private BroadcastResult broadcastToList(Collection<Node> nodeList, BaseMessage message, Node excludeNode, boolean asyn) {
        BroadcastResult result = new BroadcastResult();
        try {
            int successCount = 0;
            for (Node node : nodeList) {
                if (excludeNode != null && node.getId().equals(excludeNode.getId())) {
                    continue;
                }
                BroadcastResult br = broadcastToNode(message, node, asyn);
                if (br.isSuccess()) {
                    successCount++;
                    result.getBroadcastNodes().add(node);
                } else if (br.getErrorCode().equals(NetworkErrorCode.NET_MESSAGE_ERROR)) {
                    return br;
                }
            }
            if (successCount == 0) {
                return new BroadcastResult(false, NetworkErrorCode.NET_BROADCAST_FAIL);
            }
        } catch (Exception e) {
            return new BroadcastResult(false, NetworkErrorCode.NET_MESSAGE_ERROR);
        }
        result.setSuccess(true);
        result.setErrorCode(KernelErrorCode.SUCCESS);
        return result;
    }

    public BroadcastResult broadcastToANode(BaseMessage message, Node node, boolean asyn) {
        if (!node.isAlive() && node.getChannelId() == null) {
            return new BroadcastResult(false, NetworkErrorCode.NET_NODE_NOT_FOUND);
        }
        SocketChannel channel = NioChannelMap.get(node.getChannelId());
        if (channel == null) {
            return new BroadcastResult(false, NetworkErrorCode.NET_NODE_NOT_FOUND);
        }
        try {
            message.getHeader().setMagicNumber(networkParam.getPacketMagic());
            ChannelFuture future = channel.writeAndFlush(Unpooled.wrappedBuffer(message.serialize()));
            if (!asyn) {
                future.await();
                boolean success = future.isSuccess();
                if (!success) {
                    return new BroadcastResult(false, NetworkErrorCode.NET_BROADCAST_FAIL);
                }
            }
        } catch (Exception e) {
            Log.error(e);
            return new BroadcastResult(false, NetworkErrorCode.NET_MESSAGE_ERROR);
        }
        return new BroadcastResult(true, KernelErrorCode.SUCCESS);
    }
}
