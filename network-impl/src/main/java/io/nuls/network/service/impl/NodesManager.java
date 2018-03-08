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

import io.netty.channel.socket.SocketChannel;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.NodeDataService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.NodeTransferTool;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.service.impl.netty.NioChannelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NodesManager implements Runnable {

    private Map<String, NodeGroup> nodeGroups = new ConcurrentHashMap<>();

    private Map<String, Node> nodes = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    private List<Node> seedNodes;

    private AbstractNetworkParam network;

    private NodeDiscoverHandler discoverHandler;

    private ConnectionManager connectionManager;

    private NodeDataService nodeDao;

    private boolean running;

    private static NodesManager instance = new NodesManager();

    private NodesManager() {
    }

    public static NodesManager getInstance() {
        return instance;
    }

    /**
     * Check if is a consensus node，add consensusNodeGroup
     */
    public void init() {

        // init default NodeGroup
        NodeGroup inNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
        NodeGroup outNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
        nodeGroups.put(inNodes.getName(), inNodes);
        nodeGroups.put(outNodes.getName(), outNodes);

        boolean isConsensus = NulsContext.MODULES_CONFIG.getCfgValue(PocConsensusConstant.CFG_CONSENSUS_SECTION, PocConsensusConstant.PROPERTY_PARTAKE_PACKING, false);
        if (isConsensus) {
            NodeGroup consensusNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_CONSENSUS_GROUP);
            nodeGroups.put(consensusNodes.getName(), consensusNodes);
        }
    }

    /**
     * get nodes from database
     * connect other nodes
     * running ping/pong thread
     * running node discovery thread
     */
    public void start() {
        List<Node> nodes = discoverHandler.getLocalNodes(network.maxOutCount());
        if (nodes == null || nodes.isEmpty()) {
            nodes = getSeedNodes();
        }
        for (Node node : nodes) {
            node.setType(Node.OUT);
            node.setStatus(Node.WAIT);
            addNodeToGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP, node);
        }
        running = true;
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_NETWORK, "NetworkNodeManager", this);
        discoverHandler.start();
    }

    public List<Node> getSeedNodes() {
        if (seedNodes == null) {
            seedNodes = discoverHandler.getSeedNodes();
        }
        if (nodes.isEmpty()) {
            return seedNodes;
        } else {
            List<Node> nodeList = new ArrayList<>();
            for (Node node : seedNodes) {
                if (!nodes.containsKey(node.getId())) {
                    nodeList.add(node);
                }
            }
            return nodeList;
        }
    }

    public List<Node> getAvailableNodes() {
        List<Node> nodeList = new ArrayList<>(nodes.values());
        for (int i = nodeList.size() - 1; i >= 0; i--) {
            Node node = nodeList.get(i);
            if (!node.isHandShake()) {
                nodeList.remove(node);
            }
        }
        return nodeList;
    }

    public Node getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public void addNode(Node node) {
        lock.lock();
        try {
            if (!nodes.containsKey(node.getId())) {
                nodes.put(node.getId(), node);
                if (node.getStatus() == Node.WAIT) {
                    connectionManager.connectionNode(node);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeNode(String nodeId) {
        if (nodes.containsKey(nodeId)) {
            Node node = nodes.get(nodeId);
            //When other modules call the interface,  close channel first
            if (StringUtils.isNotBlank(node.getChannelId())) {
                SocketChannel channel = NioChannelMap.get(node.getChannelId());
                if (channel != null) {
                    channel.close();
                    return;
                }
            }

            node.destroy();
            for (String groupName : node.getGroupSet()) {
                removeNodeFromGroup(groupName, nodeId);
            }
            nodes.remove(nodeId);

            getNodeDao().removeNode(NodeTransferTool.toPojo(node));
        }
    }

    public void blackNode(String nodeId, int status) {
        if (nodes.containsKey(nodeId)) {
            Node node = nodes.get(nodeId);
            node.setStatus(status);
            getNodeDao().removeNode(NodeTransferTool.toPojo(node));

            removeNode(node.getId());
        }
    }

    public void addNodeToGroup(String groupName, Node node) {
        if (!nodeGroups.containsKey(groupName)) {
            throw new NulsRuntimeException(ErrorCode.NET_NODE_GROUP_NOT_FOUND);
        }
        NodeGroup group = nodeGroups.get(groupName);
        if (groupName.equals(NetworkConstant.NETWORK_NODE_OUT_GROUP) &&
                group.size() >= network.maxOutCount()) {
            return;
        }

        if (groupName.equals(NetworkConstant.NETWORK_NODE_IN_GROUP) &&
                group.size() >= network.maxInCount()) {
            return;
        }
        node.getGroupSet().add(group.getName());
        addNode(node);
        group.addNode(node);
    }

    public void removeNodeFromGroup(String groupName, String nodeId) {
        if (!nodeGroups.containsKey(groupName)) {
            return;
        }
        NodeGroup group = nodeGroups.get(groupName);
        group.removeNode(nodeId);
    }

    /**
     * check the nodes when closed try to connect other one
     */
    @Override
    public void run() {
        while (running) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
//            for (Node node : nodes.values()) {
//                if(node.getStatus()==2){
//                    System.out.println("-------------ip:" + node.getIp() + "-------status:" + node.getStatus() + "----------type:" + node.getType());
//                }
//            }

            if (nodes.isEmpty()) {
                List<Node> nodes = getSeedNodes();
                for (Node node : nodes) {
                    node.setType(Node.OUT);
                    node.setStatus(Node.WAIT);
                    addNodeToGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP, node);
                }
            }

            NodeGroup group = nodeGroups.get(NetworkConstant.NETWORK_NODE_OUT_GROUP);
            if (group.size() < network.maxOutCount()) {
                List<Node> nodes = discoverHandler.getLocalNodes(network.maxOutCount() - group.size());
                if (!nodes.isEmpty()) {
                    for (Node node : nodes) {
                        node.setType(Node.OUT);
                        node.setStatus(Node.WAIT);
                        addNodeToGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP, node);
                    }
                } else {
                    discoverHandler.findOtherNode(network.maxOutCount() - group.size());
                }
            }
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public NodeGroup getNodeGroup(String groupName) {
        return nodeGroups.get(groupName);
    }

    public void setNetwork(AbstractNetworkParam network) {
        this.network = network;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setDiscoverHandler(NodeDiscoverHandler discoverHandler) {
        this.discoverHandler = discoverHandler;
    }

    private NodeDataService getNodeDao() {
        if (nodeDao == null) {
            nodeDao = NulsContext.getServiceBean(NodeDataService.class);
        }
        return nodeDao;
    }
}
