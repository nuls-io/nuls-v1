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
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeConnectStatusEnum;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeStatusEnum;
import io.nuls.network.netty.broadcast.BroadcastHandler;
import io.nuls.network.netty.container.GroupContainer;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.netty.task.SaveNodeInfoTask;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.network.storage.service.NetworkStorageService;

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

    private NetworkStorageService networkStorageService;

    public static NodeManager getInstance() {
        return instance;
    }

    private NodeManager() {
        nodesContainer = new NodesContainer();
        groupContainer = new GroupContainer();
    }


    public void loadDatas() {

        //本地已经存储的节点信息
        List<Node> allNodeList = getNetworkStorageService().getAllNodes();

        // 合并种子节点
        for (Node node : allNodeList) {
            if (node.isSeedNode()) {
                node.setSeedNode(false);
            }
        }

        for (String seedId : networkParam.getSeedIpList()) {

            boolean exist = false;
            for (Node node : allNodeList) {
                if (node.getId().equals(seedId)) {
                    node.setSeedNode(true);
                    node.setStatus(NodeStatusEnum.CONNECTABLE);

                    exist = true;
                    break;
                }
            }

            if (exist) {
                continue;
            }
            try {
                String[] ipPort = seedId.split(":");
                String ip = ipPort[0];
                int port = Integer.parseInt(ipPort[1]);

                Node node = new Node(ip, port, Node.OUT);
                node.setSeedNode(true);
                node.setStatus(NodeStatusEnum.CONNECTABLE);

                allNodeList.add(node);
            } catch (Exception e) {
                Log.warn("the seed config is warn of {}", seedId);
            }
        }

        Map<String, Node> uncheckNodes = nodesContainer.getUncheckNodes();
        Map<String, Node> canConnectNodes = nodesContainer.getCanConnectNodes();
        Map<String, Node> failNodes = nodesContainer.getFailNodes();

        for (Node node : allNodeList) {
            node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
            switch (node.getStatus()) {
                case NodeStatusEnum.CONNECTABLE: {
                    canConnectNodes.put(node.getId(), node);
                    break;
                }
                case NodeStatusEnum.UNAVAILABLE: {
                    failNodes.put(node.getId(), node);
                    break;
                }
                default:
                    uncheckNodes.put(node.getId(), node);
            }
        }
    }

    public Collection<Node> getCanConnectNodes() {
        List<Node> nodeList = new ArrayList<>();

        Collection<Node> allNodes = nodesContainer.getCanConnectNodes().values();
        for (Node node : allNodes) {
            if (node.getStatus() == NodeStatusEnum.CONNECTABLE) {
                nodeList.add(node);
            }
        }
        return nodeList;
    }

    public Collection<Node> getAvailableNodes() {
        return nodesContainer.getConnectedNodes().values();
    }

    public int getAvailableNodesCount() {
        return nodesContainer.getConnectedNodes().size();
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

        node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);

        sendHandshakeMessage(node, NetworkConstant.HANDSHAKE_CLIENT_TYPE);
    }

    /**
     * 不会走到这个函数里
     *
     * @param node
     */
    public void nodeConnectFail(Node node) {
        nodesContainer.getCanConnectNodes().remove(node.getId());
        node.setStatus(NodeStatusEnum.UNAVAILABLE);
        node.setConnectStatus(NodeConnectStatusEnum.FAIL);
        nodesContainer.getFailNodes().put(node.getId(), node);
    }

    public void nodeConnectDisconnect(Node node) {

        if (node.getChannel() != null) {
            node.setChannel(null);
        }

        if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTED ||
            node.getConnectStatus() == NodeConnectStatusEnum.AVAILABLE) {

            node.setConnectStatus(NodeConnectStatusEnum.DISCONNECT);

            nodesContainer.getConnectedNodes().remove(node.getId());
        } else {
            node.setConnectStatus(NodeConnectStatusEnum.FAIL);
            node.setStatus(NodeStatusEnum.UNAVAILABLE);

            nodesContainer.getCanConnectNodes().remove(node.getId());
        }

        nodesContainer.getDisconnectNodes().put(node.getId(), node);


//        //连接断开后,判断是否是为连接成功，还是连接成功后断开
//        // 如果是未连接成功，标记为连接失败，失败次数+1，记录当前失败时间，供下次尝试连接使用
//        if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTING) {
//            node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
//            node.setStatus(NodeStatusEnum.UNAVAILABLE);
//            node.setFailCount(node.getFailCount() + 1);
//            node.setLastFailTime(TimeService.currentTimeMillis());
//            getNetworkStorageService().saveNode(node);
//        } else {
//            node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
//        }

    }

    public boolean nodeConnectIn(String ip, int port, SocketChannel channel) {
        if (!canConnectIn(ip, port)) {
            return false;
        }

        Node node = new Node(ip, port, Node.IN);
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
        node.setChannel(channel);
        cacheNode(node, channel);

        nodesContainer.getConnectedNodes().put(node.getId(), node);
        nodesContainer.markCanuseNodeByIp(ip, NodeStatusEnum.AVAILABLE);

        //监听被动连接的断开
        node.setDisconnectListener(() -> {
            nodesContainer.getConnectedNodes().remove(node.getId());
            nodesContainer.markCanuseNodeByIp(ip, NodeStatusEnum.CONNECTABLE);
        });

        sendHandshakeMessage(node, NetworkConstant.HANDSHAKE_SEVER_TYPE);
        return true;
    }

    public boolean addNeedVerifyNode(Node newNode) {
        boolean nodeExist = nodesContainer.nodeExist(newNode.getId());
        if (!nodeExist) {
            newNode.setStatus(NodeStatusEnum.UNCHECK);
            nodesContainer.getUncheckNodes().put(newNode.getId(), newNode);
        }
        return !nodeExist;
    }

    public NodesContainer getNodesContainer() {
        return nodesContainer;
    }

    public void setNodesContainer(NodesContainer nodesContainer) {
        this.nodesContainer = nodesContainer;
    }

    /**
     * 服务器被动连接规则
     * 1. 不超过最大被动连接数
     * 2. 如果自己已经主动连接了对方，不接受对方的被动连接
     * 3. 相同的IP的被动连接不超过10次
     *
     * @param ip
     * @param port
     * @return boolean
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

    private void cacheNode(Node node, SocketChannel channel) {

        String name = "node-" + node.getId();
        boolean exists = AttributeKey.exists(name);
        AttributeKey attributeKey;
        if(exists) {
            attributeKey = AttributeKey.valueOf(name);
        } else {
            attributeKey = AttributeKey.newInstance(name);
        }
        Attribute<Node> attribute = channel.attr(attributeKey);
        attribute.set(node);
    }

    private void sendHandshakeMessage(Node node, int type ) {
        NetworkMessageBody body = new NetworkMessageBody(type, networkParam.getPort(),
                NulsContext.getInstance().getBestHeight(), NulsContext.getInstance().getBestBlock().getHeader().getHash(),
                node.getIp());
        broadcastHandler.broadcastToNode(new HandshakeMessage(body), node, true);
    }


    private NetworkStorageService getNetworkStorageService() {
        if (null == this.networkStorageService) {
            this.networkStorageService = NulsContext.getServiceBean(NetworkStorageService.class);
        }
        return this.networkStorageService;
    }
}
