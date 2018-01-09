package io.nuls.network.service.impl;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.NodeDataService;
import io.nuls.network.NetworkContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.filter.impl.DefaultMessageFilter;
import io.nuls.network.message.impl.GetNodeEventHandler;
import io.nuls.network.message.filter.NulsMessageFilter;
import io.nuls.network.message.impl.NodeEventHandler;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.param.DevNetworkParam;
import io.nuls.network.param.MainNetworkParam;
import io.nuls.network.param.TestNetworkParam;
import io.nuls.network.service.Broadcaster;
import io.nuls.network.service.NetworkService;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NetworkServiceImpl implements NetworkService {

    private AbstractNetworkParam network;

    private ConnectionManager connectionManager;

    private NodesManager nodesManager;

    private Broadcaster broadcaster;


    public NetworkServiceImpl(AbstractNetworkModule module) {
        this.network = getNetworkInstance();
        NulsMessageFilter messageFilter = DefaultMessageFilter.getInstance();
        network.setMessageFilter(messageFilter);

        this.connectionManager = new ConnectionManager(module, network);
        this.nodesManager = new NodesManager(network, NulsContext.getInstance().getService(NodeDataService.class));
        this.broadcaster = new BroadcasterImpl(nodesManager, network);

        nodesManager.setConnectionManager(connectionManager);
        connectionManager.setNodesManager(nodesManager);

        GetNodeEventHandler.getInstance().setNodesManager(nodesManager);
        NodeEventHandler.getInstance().setNodesManager(nodesManager);
    }

    @Override
    public void init() {
        connectionManager.init();
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
        connectionManager.serverClose();
        TaskManager.shutdownByModuleId(NulsConstant.MODULE_ID_NETWORK);
    }

    @Override
    public void addNode(Node node) {
        nodesManager.addNode(node);
    }

    @Override
    public void removeNode(String nodeId) {
        Node node = nodesManager.getNode(nodeId);
        if (node == null) {
            throw new NulsRuntimeException(ErrorCode.NODE_NOT_FOUND);
        }
        nodesManager.deleteNode(node);
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
        nodesManager.addNodeGroup(nodeGroup);
    }


    @Override
    public BroadcastResult sendToAllNode(BaseEvent event) {
        return broadcaster.broadcast(event);
    }

    @Override
    public BroadcastResult sendToAllNode(BaseEvent event, String excludeNodeId) {
        return broadcaster.broadcast(event, excludeNodeId);
    }

    @Override
    public BroadcastResult sendToAllNode(byte[] data) {
        return broadcaster.broadcast(data);
    }

    @Override
    public BroadcastResult sendToAllNode(byte[] data, String excludeNodeId) {
        return broadcaster.broadcast(data, excludeNodeId);
    }

    @Override
    public BroadcastResult sendToNode(BaseEvent event, String nodeId) {
        return broadcaster.broadcastToNode(event, nodeId);
    }

    @Override
    public BroadcastResult sendToNode(byte[] data, String nodeId) {
        return broadcaster.broadcastToNode(data, nodeId);
    }

    @Override
    public BroadcastResult sendToGroup(BaseEvent event, String groupName) {
        return broadcaster.broadcastToGroup(event, groupName);
    }

    @Override
    public BroadcastResult sendToGroup(BaseEvent event, String groupName, String excludeNodeId) {
        return broadcaster.broadcastToGroup(event, groupName, excludeNodeId);
    }

    @Override
    public BroadcastResult sendToGroup(byte[] data, String groupName) {
        return broadcaster.broadcastToGroup(data, groupName);
    }

    @Override
    public BroadcastResult sendToGroup(byte[] data, String groupName, String excludeNodeId) {
        return broadcaster.broadcastToGroup(data, groupName, excludeNodeId);
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

