/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.network.netty.broadcast;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.BroadcastResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.netty.manager.NodeManager;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.MessageHeader;

import java.io.IOException;
import java.util.*;

public class BroadcastHandler {

    private static BroadcastHandler instance = new BroadcastHandler();

    private BroadcastHandler() {

    }

    public static BroadcastHandler getInstance() {
        return instance;
    }

    private NetworkParam networkParam = NetworkParam.getInstance();

    private NodeManager nodeManager = NodeManager.getInstance();

    public BroadcastResult broadcastToAllNode(BaseMessage msg, Node excludeNode, boolean asyn, int percent) {
        Collection<Node> nodeList = NodeManager.getInstance().getAvailableNodes();
        if (nodeList == null || nodeList.isEmpty()) {
            Log.error("node list is null");
            return new BroadcastResult(false, NetworkErrorCode.NET_BROADCAST_NODE_EMPTY);
        }
        return broadcastToList(nodeList, msg, excludeNode, asyn, percent);
    }

    public BroadcastResult broadcastToHalfNode(BaseMessage msg, Node excludeNode, boolean asyn) {
        Collection<Node> nodes = nodeManager.getAvailableNodes();
        if (nodes.isEmpty()) {
            return new BroadcastResult(false, NetworkErrorCode.NET_BROADCAST_NODE_EMPTY);
        }
        List<Node> nodeList = new ArrayList<>();
        int i = 0;
        for (Node node : nodes) {
            i++;
            if (i % 2 == 1) {
                nodeList.add(node);
            }
        }

        return broadcastToList(nodeList, msg, excludeNode, asyn, 50);
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
        return broadcastToList(group.getNodes().values(), msg, null, asyn, 100);
    }

    public BroadcastResult broadcastToNodeGroup(BaseMessage msg, String groupName, Node excludeNode, boolean asyn) {
        NodeGroup group = nodeManager.getNodeGroup(groupName);
        if (group == null || group.size() == 0) {
            return new BroadcastResult(false, NetworkErrorCode.NET_BROADCAST_NODE_EMPTY);
        }
        return broadcastToList(group.getNodes().values(), msg, excludeNode, asyn, 100);
    }

    private BroadcastResult broadcastToList(Collection<Node> nodeList, BaseMessage message, Node excludeNode, boolean asyn, int percent) {
        BroadcastResult result = new BroadcastResult();
        try {
            int successCount = 0;
            int minCount = 5;
            //根据百分比决定直接广播给多少个节点
            if (nodeList.size() > minCount && percent < 100) {
                int needCount = nodeList.size() * percent / 100;
                if (needCount < minCount) {
                    needCount = minCount;
                }
                Set<Integer> set = new HashSet<>();
                while (true) {
                    Random rand = new Random();
                    int ran = rand.nextInt(nodeList.size());
                    set.add(ran);
                    if (set.size() == needCount + 1) {
                        break;
                    }
                }

                int nodeListIndex = 0;
                Collection<Node> nodeBroadcastList = new ArrayList<>();
                for (Node node : nodeList) {
                    if (set.contains(nodeListIndex)) {
                        if (excludeNode != null && node.getId().equals(excludeNode.getId())) {
                            nodeListIndex++;
                            continue;
                        }
                        nodeBroadcastList.add(node);
                        if (nodeBroadcastList.size() == needCount) {
                            break;
                        }
                    }
                    nodeListIndex++;
                }
                nodeList = nodeBroadcastList;
            }
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
        if (!node.isAlive()) {
            return new BroadcastResult(false, NetworkErrorCode.NET_NODE_DEAD);
        }
        if (node.getChannel() == null || !node.getChannel().isActive()) {
            return new BroadcastResult(false, NetworkErrorCode.NET_NODE_MISS_CHANNEL);
        }
        try {
            MessageHeader header = message.getHeader();
            header.setMagicNumber(networkParam.getPacketMagic());

            BaseNulsData body = message.getMsgBody();
            header.setLength(body.size());

            if(asyn) {
                node.getChannel().eventLoop().execute(() -> {
                    try {
                        Channel channel = node.getChannel();
                        if (channel != null) {
                            channel.writeAndFlush(Unpooled.wrappedBuffer(message.serialize()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                ChannelFuture future = node.getChannel().writeAndFlush(Unpooled.wrappedBuffer(message.serialize()));
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
