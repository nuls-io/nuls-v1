package io.nuls.network.service.impl;


import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.network.IpUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.NodeDataService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.NodeTransfer;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.entity.PingEvent;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NodesManager implements Runnable {

    private AbstractNetworkParam network;

    private NodeDiscoverHandler discovery;

    private ConnectionManager connectionManager;

    private static Map<String, NodeGroup> nodeGroups = new ConcurrentHashMap<>();

    private static Map<String, Node> nodes = new ConcurrentHashMap<>();

    private NodeDataService nodeDao;

    private ReentrantLock lock;

    private List<Node> seedNodes;

    private boolean running;

    public NodesManager(AbstractNetworkParam network, NodeDataService nodeDao) {
        this.network = network;
        this.nodeDao = nodeDao;
        lock = new ReentrantLock();
        // the default nodeGroups
        NodeGroup inNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
        NodeGroup outNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
        NodeGroup consensusNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_CONSENSUS_GROUP);

        nodeGroups.put(inNodes.getName(), inNodes);
        nodeGroups.put(outNodes.getName(), outNodes);
        nodeGroups.put(consensusNodes.getName(), consensusNodes);

        this.discovery = new NodeDiscoverHandler(this, network, nodeDao);
    }

    /**
     * 1. get nodes from database
     * start p2p discovery thread
     * start a nodes server
     * query config find original nodes
     * query database find cached nodes
     * find other nodes from connetcted nodes
     */
    public void start() {
        running = true;
        List<Node> nodes = discovery.getLocalNodes(10);
        if (nodes.isEmpty()) {
            nodes = getSeedNodes();
        }

        for (Node node : nodes) {
            node.setType(Node.OUT);
            addNodeToGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP, node);
        }

        boolean isConsensus = NulsContext.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_PARTAKE_PACKING, false);
        if (isConsensus) {
            network.maxOutCount(network.maxOutCount() * 2);
            network.maxInCount(network.maxInCount() * 2);
        }

        System.out.println("-----------nodeManager start");
        //start  heart beat thread
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_NETWORK, "nodeManager", this);
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_NETWORK, "nodeDiscovery", this.discovery);
    }

    public void stop() {
        running = false;
    }

    public void destory() {
        stop();
    }

    public List<Node> getSeedNodes() {
        if (seedNodes == null) {
            seedNodes = discovery.getSeedNodes();
        }
        return seedNodes;
    }

    /**
     * when nodeId is null, check myself
     *
     * @param nodeId
     * @return
     */
    public boolean isSeed(String nodeId) {
        if (StringUtils.isBlank(nodeId)) {
            Set<String> ips = IpUtil.getIps();
            for (String self : ips) {
                for (Node node : getSeedNodes()) {
                    if (node.getHash().equals(self)) {
                        return true;
                    }
                }
            }
        } else {
            for (Node node : getSeedNodes()) {
                if (node.getHash().equals(nodeId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addNode(Node node) {
        lock.lock();
        try {
            if (!nodes.containsKey(node.getHash().toString())) {
                nodes.put(node.getHash(), node);
                if (!node.isHandShake() && node.getType() == Node.OUT) {
                    connectionManager.openConnection(node);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void addNodeToGroup(String groupName, Node node) {
        lock.lock();
        try {
            if (!nodeGroups.containsKey(groupName)) {
                throw new NulsRuntimeException(ErrorCode.NODE_GROUP_NOT_FOUND);
            }
            if (groupName.equals(NetworkConstant.NETWORK_NODE_OUT_GROUP) &&
                    nodeGroups.get(groupName).size() >= network.maxOutCount()) {
                return;
            }
            if (groupName.equals(NetworkConstant.NETWORK_NODE_IN_GROUP) &&
                    nodeGroups.get(groupName).size() >= network.maxInCount()) {
                return;
            }

            addNode(node);
            nodeGroups.get(groupName).addNode(node);
        } finally {
            lock.unlock();
        }
    }

    public void removeNode(String nodeHash) {
        lock.lock();
        try {
            if (nodes.containsKey(nodeHash)) {
                for (NodeGroup group : nodeGroups.values()) {
                    for (Node node : group.getNodes()) {
                        if (node.getHash().equals(nodeHash)) {
                            group.removeNode(node);
                            break;
                        }
                    }
                }
                if (!isSeed(nodeHash)) {
                    Node node = nodes.get(nodeHash);
                    node.setFailCount(node.getFailCount() + 1);
                    nodeDao.saveChange(NodeTransfer.toPojo(node));
                }
                nodes.remove(nodeHash);
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeNodeFromGroup(String groupName, String nodeId) {
        lock.lock();
        try {
            if (!nodeGroups.containsKey(groupName)) {
                throw new NulsRuntimeException(ErrorCode.NODE_GROUP_NOT_FOUND);
            }
            Node node = getNode(nodeId);
            if (node == null) {
                throw new NulsRuntimeException(ErrorCode.NODE_NOT_FOUND);
            }
            nodeGroups.get(groupName).removeNode(node);
        } finally {
            lock.unlock();
        }
    }


    public boolean hasNodeGroup(String groupName) {
        return nodeGroups.containsKey(groupName);
    }

    public void addNodeGroup(NodeGroup nodeGroup) {
        if (nodeGroups.containsKey(nodeGroup.getName())) {
            throw new NulsRuntimeException(ErrorCode.NODE_GROUP_ALREADY_EXISTS);
        }
        nodeGroups.put(nodeGroup.getName(), nodeGroup);
    }

    public void destroyNodeGroup(String groupName) {
        lock.lock();
        try {
            if (!nodeGroups.containsKey(groupName)) {
                return;
            }

            NodeGroup group = nodeGroups.get(groupName);
            for (Node p : group.getNodes()) {
                p.destroy();
                group.removeNode(p);
            }
            nodeGroups.remove(groupName);
        } finally {
            lock.unlock();
        }
    }

    /**
     * remove from database
     *
     * @param node
     */
    public void deleteNode(Node node) {
        node.destroy();
        nodeDao.delete(node.getHash());
    }

    public Node getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public NodeGroup getNodeGroup(String groupName) {
        return nodeGroups.get(groupName);
    }


    public List<Node> getAvailableNodesByGroup(String groupName) {
        List<Node> availableNodes = new ArrayList<>();
        if (hasNodeGroup(groupName)) {
            for (Node node : getNodeGroup(groupName).getNodes()) {
                if (node.getStatus() == Node.HANDSHAKE) {
                    availableNodes.add(node);
                }
            }
        }
        return availableNodes;
    }


    public List<Node> getAvailableNodes(String excludeNodeId) {
        List<Node> availableNodes = new ArrayList<>();
        Collection<Node> collection = nodes.values();
        for (Node node : collection) {
            if (node.getStatus() == Node.HANDSHAKE && !node.getIp().equals(excludeNodeId)) {
                availableNodes.add(node);
            }
        }
        return availableNodes;
    }

    public List<Node> getAvailableNodes(int size, Node excludeNode) {
        List<Node> availableNodes = getAvailableNodes(excludeNode.getHash());
        if (availableNodes.size() <= size) {
            return availableNodes;
        }
        Collections.shuffle(availableNodes);
        return availableNodes.subList(0, size);
    }

    public int getBroadcasterMinConnectionCount() {
        int count = 0;
        Collection<Node> collection = nodes.values();
        for (Node node : collection) {
            if (node.getStatus() == Node.HANDSHAKE) {
                count++;
            }
        }
        if (count <= 1) {
            return count;
        } else {
            return Math.max(1, (int) (count * 0.8));
        }
    }

    public List<Node> getGroupAvailableNodes(String groupName, String excludeNodeId) {
        if (!nodeGroups.containsKey(groupName)) {
            throw new NulsRuntimeException(ErrorCode.NODE_GROUP_NOT_FOUND);
        }
        List<Node> availableNodes = new ArrayList<>();
        NodeGroup group = nodeGroups.get(groupName);
        for (Node node : group.getNodes()) {
            if (node.getStatus() == Node.HANDSHAKE && !node.getIp().equals(excludeNodeId)) {
                availableNodes.add(node);
            }
        }
        return availableNodes;
    }


    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public void run() {

        while (running) {
            for (Node node : nodes.values()) {
                if (node.getStatus() == Node.HANDSHAKE) {
                    PingEvent ping = new PingEvent();
                    try {
                        node.sendNetworkEvent(ping);
                    } catch (IOException e) {
                        Log.error(e);
                        node.destroy();
                    }
                }
            }

            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {

            }
        }
    }
}
