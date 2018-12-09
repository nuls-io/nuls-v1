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

package io.nuls.network.netty.manager;

import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.listener.EventListener;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.netty.broadcast.BroadcastHandler;
import io.nuls.network.netty.container.GroupContainer;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NodeManager {

    private final BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private static NodeManager instance = new NodeManager();

    private NetworkParam networkParam;

    private NodesContainer nodesContainer;
    private GroupContainer groupContainer;

    public static NodeManager getInstance() {
        return instance;
    }

    private NodeManager() {
        nodesContainer = new NodesContainer();
        groupContainer = new GroupContainer();
    }


    public void loadDatas() {

        Map<String, Node> allNodes = nodesContainer.getCanConnectNodes();

        //加载数据库的节点信心 TODO


        // 合并种子节点
        for (Map.Entry<String, Node> nodeEntry : allNodes.entrySet()) {
            Node node = nodeEntry.getValue();
            node.setCanConnect(true);

            if (node.isSeedNode()) {
                node.setSeedNode(false);
            }
        }
        for (String seedId : networkParam.getSeedIpList()) {

            if (allNodes.containsKey(seedId)) {
                allNodes.get(seedId).setSeedNode(true);
                continue;
            }
            try {
                String[] ipPort = seedId.split(":");
                String ip = ipPort[0];
                int port = Integer.parseInt(ipPort[1]);

                Node node = new Node(ip, port, Node.OUT);
                node.setCanConnect(true);

                allNodes.put(seedId, node);
            } catch (Exception e) {
                Log.warn("the seed config is warn of {}", seedId);
            }
        }
    }

    public Collection<Node> getAvailableNodes() {
        return nodesContainer.getConnectedNodes().values();
    }

    public int getAvailableNodesCount() {
        return nodesContainer.getConnectedNodes().size();
    }

    public Collection<Node> getCanConnectNodes() {
        List<Node> nodeList = new ArrayList<>();

        Collection<Node> allNodes = nodesContainer.getCanConnectNodes().values();
        for (Node node : allNodes) {
            if (node.isCanConnect()) {
                nodeList.add(node);
            }
        }
        return nodeList;
    }

    public NodeGroup getNodeGroup(String groupName) {
        return groupContainer.getNodeGroup(groupName);
    }

    public Map<String, Node> getNodes() {
        return nodesContainer.getConnectedNodes();
    }

    public Node getNode(String nodeId) {
        return nodesContainer.getNode(nodeId);
    }

    public void removeNode(String nodeId) {
        return;
    }

    public void reset() {
    }

    public void initNetworkParam(NetworkParam networkParam) {
        this.networkParam = networkParam;
    }

    public void nodeConnectSuccess(Node node) {
        nodesContainer.getConnectedNodes().put(node.getId(), node);
        nodesContainer.getCanConnectNodes().remove(node.getId());

        node.setConnectStatus(Node.CONNECT);

        sendHandshakeMessage(node);
    }


    public void nodeConnectFail(Node node) {
        nodesContainer.getCanConnectNodes().remove(node.getId());
        node.setCanConnect(false);
        nodesContainer.getFailNodes().put(node.getId(), node);
    }

    public void nodeConnectDisconnect(Node node) {
        nodesContainer.getConnectedNodes().remove(node.getId());
        nodesContainer.getDisconnectNodes().put(node.getId(), node);

        if (node.getChannel() != null) {
            node.setChannel(null);
        }
    }

    public boolean nodeConnectIn(String ip, int port, SocketChannel channel) {
        if (!canConnectIn(ip, port)) {
            return false;
        }

        Node node = new Node(ip, port, Node.IN);
        node.setConnectStatus(Node.CONNECT);
        node.setChannel(channel);
        Attribute<Node> attribute = channel.attr(AttributeKey.newInstance("node-" + node.getId()));
        attribute.set(node);

        nodesContainer.getConnectedNodes().put(node.getId(), node);
        nodesContainer.markCanuseNodeByIp(ip);

        //监听被动连接的断开
        node.setDisconnectListener(new EventListener() {
            @Override
            public void action() {
                nodesContainer.getConnectedNodes().remove(node.getId());
            }
        });

        sendHandshakeMessage(node);
        return true;
    }

    public void addNeedVerifyNode(Node newNode) {
        // TODO 需要把待验证的区分，这里就先放可用里面了
        Map<String, Node> canConnectNodeMap = nodesContainer.getCanConnectNodes();
        if (canConnectNodeMap.containsKey(newNode.getId())) {
            return;
        }
        newNode.setCanConnect(true);
        canConnectNodeMap.put(newNode.getId(), newNode);
    }

    /**
     * 服务器被动连接规则
     * 1. 不超过最大被动连接数
     * 2. 如果自己已经主动连接了对方，不接受对方的被动连接
     * 3. 相同的IP的被动连接不超过10次
     *
     * @param ip
     * @param port
     * @return
     */
    private boolean canConnectIn(String ip, int port) {
        int size = groupContainer.getNodesCount(NetworkConstant.NETWORK_NODE_IN_GROUP);

        if (size >= networkParam.getMaxInCount()) {
            return false;
        }

        Map<String, Node> connectedNodes = nodesContainer.getConnectedNodes();

        int sameIpCount = 0;

        for (Node node : connectedNodes.values()) {
            //不会存在两次被动连接都是同一个端口的，即使是同一台服务器
            //if(ip.equals(node.getIp()) && (node.getPort().intValue() == port || node.getType() == Node.OUT)) {
            if (ip.equals(node.getIp()) && node.getType() == Node.OUT) {
                return false;
            }
            if (ip.equals(node.getIp())) {
                sameIpCount++;
            }
            if (sameIpCount >= NetworkConstant.SAME_IP_MAX_COUNT) {
                return false;
            }
        }

        return true;
    }

    private void sendHandshakeMessage(Node node) {
        NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, networkParam.getPort(),
                NulsContext.getInstance().getBestHeight(), NulsContext.getInstance().getBestBlock().getHeader().getHash(),
                node.getIp());
        broadcastHandler.broadcastToNode(new HandshakeMessage(body), node, true);
    }
}
