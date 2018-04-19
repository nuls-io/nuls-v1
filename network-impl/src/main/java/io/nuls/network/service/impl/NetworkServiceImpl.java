/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.network.IpUtil;
import io.nuls.network.NetworkContext;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.filter.impl.DefaultMessageFilter;
import io.nuls.network.message.entity.VersionEvent;
import io.nuls.network.message.filter.MessageFilterChain;
import io.nuls.network.message.filter.NulsMessageFilter;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.base.BaseEvent;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkServiceImpl implements NetworkService {

    private NetworkParam network;

    private ConnectionManager connectionManager;

    private NodesManager nodesManager;

    private BroadcastHandler broadcaster;

    public NetworkServiceImpl() {
        this.network = NetworkParam.getInstance();

        DefaultMessageFilter.getInstance().addMagicNum(network.getPacketMagic());
        MessageFilterChain.getInstance().addFilter(DefaultMessageFilter.getInstance());
        NulsContext.setMagicNumber(network.getPacketMagic());

        NulsMessageFilter messageFilter = DefaultMessageFilter.getInstance();
        network.setMessageFilter(messageFilter);

        this.connectionManager = ConnectionManager.getInstance();
        connectionManager.setNetwork(network);
        connectionManager.setNetworkService(this);

        this.nodesManager = NodesManager.getInstance();
        nodesManager.setNetwork(network);
        nodesManager.setConnectionManager(connectionManager);

        this.broadcaster = BroadcastHandler.getInstance();
        broadcaster.setNetwork(network);
        broadcaster.setNodesManager(nodesManager);

        NodeDiscoverHandler discoverHandler = NodeDiscoverHandler.getInstance();
        discoverHandler.setNetwork(network);
        discoverHandler.setNodesManager(nodesManager);
        discoverHandler.setBroadcaster(broadcaster);
        nodesManager.setDiscoverHandler(discoverHandler);
    }

    @Override
    public void init() {
        try {
            connectionManager.init();
            nodesManager.init();
            for (String ip : IpUtil.getIps()) {
                if (isSeedNode(ip)) {
                    network.setMaxInCount(network.getMaxInCount() * 2);
                    nodesManager.setSeed(true);
                }
            }
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.NET_SERVER_START_ERROR);
        }
    }

    @Override
    public void start() {
        try {
            connectionManager.start();
            nodesManager.start();
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(ErrorCode.NET_SERVER_START_ERROR);
        }
    }

    @Override
    public void shutdown() {
        TaskManager.shutdownByModuleId(NulsConstant.MODULE_ID_NETWORK);
    }

    @Override
    public void removeNode(String nodeId) {
        nodesManager.removeNode(nodeId);
    }

    @Override
    public void removeNode(String nodeId, int type) {
        System.out.println("----------removeHandshakeNode node------------");
        nodesManager.removeHandshakeNode(nodeId);
    }

    @Override
    public void deleteNode(String nodeId) {
        nodesManager.deleteNode(nodeId);
    }

    @Override
    public Map<String, Node> getNodes() {
        return nodesManager.getNodes();
    }

    @Override
    public Node getNode(String nodeId) {
        return nodesManager.getNode(nodeId);
    }

    @Override
    public Collection<Node> getAvailableNodes() {
        return nodesManager.getAvailableNodes();
    }

    @Override
    public Set<String> getNodesIp() {
        Set<String> ipList = new HashSet<>();
        for (String ip : NetworkContext.ipMap.keySet()) {
            ipList.add(ip);
        }
        Collection<Node> nodeList = getAvailableNodes();
        for (Node node : nodeList) {
            ipList.add(node.getIp());
        }
        return ipList;
    }

    @Override
    public boolean addNode(Node node) {
        return nodesManager.addNode(node);
    }

    @Override
    public boolean addConnNode(Node node) {
        return nodesManager.addConnNode(node);
    }

    @Override
    public boolean isSeedNode(String ip) {
        return nodesManager.isSeedNode(ip);
    }

    @Override
    public void saveNode(Node node) {
        nodesManager.saveNode(node);
    }

    @Override
    public boolean isSeed() {
        return nodesManager.isSeed();
    }

    @Override
    public boolean handshakeNode(String groupName, Node node, VersionEvent versionEvent) {
        return nodesManager.handshakeNode(groupName, node, versionEvent);
    }

    @Override
    public void blackNode(String nodeId, int status) {
        nodesManager.blackNode(nodeId, status);
    }

    @Override
    public boolean addNodeToGroup(String groupName, Node node) {
        return nodesManager.addNodeToGroup(groupName, node);
    }

    @Override
    public void removeNodeFromGroup(String groupName, String nodeId) {
        nodesManager.removeNodeFromGroup(groupName, nodeId);
    }

    @Override
    public void addNodeGroup(NodeGroup nodeGroup) {
        //todo
    }

    @Override
    public void removeNodeGroup(String groupName) {
        //todo
    }

    @Override
    public NodeGroup getNodeGroup(String groupName) {
        return nodesManager.getNodeGroup(groupName);
    }

    @Override
    public NetworkParam getNetworkParam() {
        return network;
    }


    @Override
    public BroadcastResult sendToAllNode(BaseEvent event, boolean asyn) {
        return broadcaster.broadcast(event, asyn);
    }

    @Override
    public BroadcastResult sendToAllNode(BaseEvent event, String excludeNodeId, boolean asyn) {
        return broadcaster.broadcast(event, excludeNodeId, asyn);
    }

    @Override
    public BroadcastResult sendToNode(BaseEvent event, String nodeId, boolean asyn) {
        return broadcaster.broadcastToNode(event, nodeId, asyn);
    }

    @Override
    public BroadcastResult sendToGroup(BaseEvent event, String groupName, boolean asyn) {
        return broadcaster.broadcastToGroup(event, groupName, asyn);
    }

    @Override
    public BroadcastResult sendToGroup(BaseEvent event, String groupName, String excludeNodeId, boolean asyn) {
        return broadcaster.broadcastToGroup(event, groupName, excludeNodeId, asyn);
    }

    @Override
    public void receiveMessage(ByteBuffer buffer, Node node) {
        connectionManager.receiveMessage(buffer, node);
    }

    @Override
    public void reset() {
        nodesManager.reset();
    }

    @Override
    public void validateFirstUnConnectedNode(String nodeId) {
        nodesManager.validateFirstUnConnectedNode(nodeId);
    }

}

