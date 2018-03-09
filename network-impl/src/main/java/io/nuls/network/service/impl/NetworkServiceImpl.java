/**
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
 */
package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.network.NetworkContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.filter.impl.DefaultMessageFilter;
import io.nuls.network.message.filter.MessageFilterChain;
import io.nuls.network.message.filter.NulsMessageFilter;
import io.nuls.network.param.DevNetworkParam;
import io.nuls.network.param.MainNetworkParam;
import io.nuls.network.param.TestNetworkParam;
import io.nuls.network.service.NetworkService;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkServiceImpl implements NetworkService {

    private AbstractNetworkParam network;

    private ConnectionManager connectionManager;

    private NodesManager nodesManager;

    private BroadcastHandler broadcaster;

    public NetworkServiceImpl() {
        this.network = getNetworkInstance();
        DefaultMessageFilter.getInstance().addMagicNum(network.packetMagic());
        MessageFilterChain.getInstance().addFilter(DefaultMessageFilter.getInstance());
        NulsContext.setMagicNumber(network.packetMagic());

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
    public Node getNode(String nodeId) {
        return nodesManager.getNode(nodeId);
    }

    @Override
    public List<Node> getAvailableNodes() {
        return nodesManager.getAvailableNodes();
    }

    @Override
    public void blackNode(String nodeId, int status) {
        nodesManager.blackNode(nodeId, status);
    }

    @Override
    public void addNodeToGroup(String groupName, Node node) {
        nodesManager.addNodeToGroup(groupName, node);
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
    public AbstractNetworkParam getNetworkParam() {
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

    private AbstractNetworkParam getNetworkInstance() {
        String networkType = NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_TYPE, "dev");
        if ("dev".equals(networkType)) {
            return DevNetworkParam.get();
        }
        if ("test".equals(networkType)) {
            return TestNetworkParam.get();
        }

        return MainNetworkParam.get();
    }
}

