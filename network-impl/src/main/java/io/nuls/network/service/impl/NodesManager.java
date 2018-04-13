/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
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
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.network.IpUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.NodeDataService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.NodeTransferTool;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.service.impl.netty.NioChannelMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NodesManager implements Runnable {

    private Map<String, NodeGroup> nodeGroups = new ConcurrentHashMap<>();

    private Map<String, Node> disConnectNodes = new ConcurrentHashMap<>();

    private Map<String, Node> connectedNodes = new ConcurrentHashMap<>();

    private Map<String, Node> handShakeNodes = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    private AbstractNetworkParam network;

    private NodeDiscoverHandler discoverHandler;

    private ConnectionManager connectionManager;

    private NodeDataService nodeDao;

    private boolean running;

    private boolean isSeed;

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
//        List<Node> nodes;
//        if (isSeed) {
//            nodes = getSeedNodes();
//        } else {
//            nodes = discoverHandler.getLocalNodes();
//            if (nodes.size() < network.maxOutCount() / 2) {
//                int size = network.maxOutCount() / 2 - nodes.size();
//                int count = 0;
//                for (Node node : getSeedNodes()) {
//                    addNode(node);
//                    count++;
//                    if (count == size) {
//                        break;
//                    }
//                }
//            }
//        }
//        for (Node node : nodes) {
//            addNode(node);
//        }

        running = true;
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_NETWORK, "NetworkNodeManager", this);
        discoverHandler.start();
    }

    public void reset() {
        System.out.println("------------------nodeManager reset--------------------");
        for (Node node : disConnectNodes.values()) {
            node.setFailCount(NetworkConstant.FAIL_MAX_COUNT);
        }
        for (Node node : handShakeNodes.values()) {
            removeNode(node);
        }
    }


    public boolean addNode(Node node) {
        if (IpUtil.getIps().contains(node.getIp())) {
            return false;
        }
        lock.lock();
        try {
            if (!disConnectNodes.containsKey(node.getId()) &&
                    !connectedNodes.containsKey(node.getId()) &&
                    !handShakeNodes.containsKey(node.getId())) {
                Map<String, Node> nodeMap = getNodes();
                for (Node n : nodeMap.values()) {
                    if (n.getIp().equals(node.getIp())) {
                        return false;
                    }
                }
                connectionManager.connectionNode(node);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean addConnNode(Node node) {
        lock.lock();
        try {
            if (!connectedNodes.containsKey(node.getId()) && !handShakeNodes.containsKey(node.getId())) {
                disConnectNodes.remove(node.getId());
                connectedNodes.put(node.getId(), node);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void addFailNode(Node node) {
        if (IpUtil.getIps().contains(node.getIp())) {
            return;
        }
        lock.lock();
        try {
            node.destroy();
            if (!disConnectNodes.containsKey(node.getId()) &&
                    !connectedNodes.containsKey(node.getId())) {
                Map<String, Node> nodeMap = getNodes();
                for (Node n : nodeMap.values()) {
                    if (n.getIp().equals(node.getIp())) {
                        return;
                    }
                }
                removeNodeHandler(node);

            }
        } finally {
            lock.unlock();
        }
    }

    public boolean addNodeToGroup(String groupName, Node node) {
        NodeGroup nodeGroup = nodeGroups.get(groupName);
        if (nodeGroup == null) {
            //todo  throw new NulsExcetpion
//            throw new RuntimeException("group not found");
            return false;
        }
        if (groupName.equals(NetworkConstant.NETWORK_NODE_IN_GROUP) && nodeGroup.size() >= network.maxInCount()) {
            return false;
        }
        if (groupName.equals(NetworkConstant.NETWORK_NODE_OUT_GROUP) && nodeGroup.size() >= network.maxOutCount()) {
            return false;
        }
        node.addGroup(groupName);
        nodeGroup.addNode(node);
        return true;
    }

    public Node getNode(String nodeId) {
        Node node = disConnectNodes.get(nodeId);
        if (node == null) {
            node = connectedNodes.get(nodeId);
        }
        if (node == null) {
            node = handShakeNodes.get(nodeId);
        }
        return node;
    }

    public Map<String, Node> getNodes() {
        Map<String, Node> nodeMap = new HashMap<>();
        nodeMap.putAll(disConnectNodes);
        nodeMap.putAll(connectedNodes);
        nodeMap.putAll(handShakeNodes);
        return nodeMap;
    }

    public List<Node> getSeedNodes() {
        List<Node> seedNodes = new ArrayList<>();

        Set<String> localIp = IpUtil.getIps();
        for (String ip : network.getSeedIpList()) {
            if (!localIp.contains(ip)) {
                seedNodes.add(new Node(ip, network.port(), network.port(), Node.OUT));
            }
        }
        return seedNodes;
    }

    public void removeNode(String nodeId) {
        Node node = getNode(nodeId);
        if (node != null) {
            removeNode(node);
        } else {
            //todo  remove database if it had

            //nodeDao.removeNode();
        }
    }

    public void removeNode(Node node) {
        lock.lock();
        try {
            if (StringUtils.isNotBlank(node.getChannelId())) {
                SocketChannel channel = NioChannelMap.get(node.getChannelId());
                if (channel != null) {
                    channel.close();
                    return;
                }
            }
            node.destroy();
            removeNodeFromGroup(node);
            removeNodeHandler(node);
        } finally {
            lock.unlock();
        }
    }

    private void removeNodeHandler(Node node) {
        if (node.getType() == Node.BAD || node.getType() == Node.IN) {
            disConnectNodes.remove(node.getId());
            connectedNodes.remove(node.getId());
            handShakeNodes.remove(node.getId());
            if (node.getStatus() == Node.BAD) {
                //todo  remove database
            }
            return;
        }

        if (connectedNodes.containsKey(node.getId())) {
            connectedNodes.remove(node.getId());
        }
        if (handShakeNodes.containsKey(node.getId())) {
            handShakeNodes.remove(node.getId());
        }

        // 哪些情况会在dis里加入node
        if (!disConnectNodes.containsKey(node.getId())) {
            disConnectNodes.put(node.getId(), node);
        }

        if (node.getFailCount() <= NetworkConstant.FAIL_MAX_COUNT) {
            node.setLastFailTime(System.currentTimeMillis() + 5 * 1000 * node.getFailCount());
        } else {
            //todo remove database
        }
    }

    private void removeNodeFromGroup(Node node) {
        for (String groupName : node.getGroupSet()) {
            NodeGroup group = nodeGroups.get(groupName);
            if (group != null) {
                group.removeNode(node.getId());
            }
        }
        node.getGroupSet().clear();
    }

    public void deleteNode(String nodeId) {
        disConnectNodes.remove(nodeId);
    }

    private void removeNodeFromGroup(Node node, String groupName) {
        NodeGroup group = nodeGroups.get(groupName);
        if (group != null) {
            group.removeNode(node.getId());
        }
        node.getGroupSet().remove(groupName);
    }


    private int getNodeFromDataBase(int size) {
        return 0;
    }

    private void getNodeFromOther(int size) {
        discoverHandler.findOtherNode(size);
    }

    /**
     * @param node
     * @return
     */
    public boolean handshakeNode(String groupName, Node node) {
        lock.lock();
        try {
            if (!checkFullHandShake(node)) {
                return false;
            }
            if (!connectedNodes.containsKey(node.getId())) {
                return false;
            }
            connectedNodes.remove(node.getId());
            node.setStatus(Node.HANDSHAKE);
            handShakeNodes.put(node.getId(), node);

            return addNodeToGroup(groupName, node);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param node
     * @return
     */
    private boolean checkFullHandShake(Node node) {
        if (node.getType() == Node.IN) {
            NodeGroup inGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
            return inGroup.size() < network.maxInCount();
        } else {
            NodeGroup outGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
            return outGroup.size() < network.maxOutCount();
        }
    }

    /**
     * -------------------------------华丽的分割线---------------------------
     */


    public Collection<Node> getAvailableNodes() {
        return handShakeNodes.values();
    }

    public List<Node> getConnectNode() {
        List<Node> nodeList = new ArrayList<>();
        for (Node node : disConnectNodes.values()) {
            if (node.isAlive()) {
                nodeList.add(node);
            }
        }
        for (Node node : connectedNodes.values()) {
            if (node.isAlive()) {
                nodeList.add(node);
            }
        }
        return nodeList;
    }

    public void blackNode(String nodeId, int status) {
//        if (nodes.containsKey(nodeId)) {
//            Node node = nodes.get(nodeId);
//            node.setStatus(status);
//            node.setFailCount(20);
//            getNodeDao().removeNode(NodeTransferTool.toPojo(node));
//
//            removeNode(node.getId(), null);
//        }
    }


    public void removeNodeFromGroup(String groupName, String nodeId) {
        if (!nodeGroups.containsKey(groupName)) {
            return;
        }
        nodeGroups.get(groupName).removeNode(nodeId);
    }


    public boolean isSeedNode(String ip) {
        return network.getSeedIpList().contains(ip);
    }

    /**
     * check the nodes when closed try to connect other one
     */

    int count = 0;

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (running) {
            count++;
            if (count == 2) {
                count = 0;
                System.out.println("disConnectNodes:");
                for (Node node : disConnectNodes.values()) {
                    System.out.println(node.toString());
                }
                System.out.println();
                System.out.println("connectedNodes:");
                for (Node node : connectedNodes.values()) {
                    System.out.println(node.toString());
                }
                System.out.println();
                System.out.println("handShakeNodes:");
                for (Node node : handShakeNodes.values()) {
                    Log.info(node.toString() + ",blockHeight:" + node.getVersionMessage().getBestBlockHeight());
                }
            }

            if (connectedNodes.isEmpty() && handShakeNodes.size() <= 2) {
                List<Node> seedNodes = getSeedNodes();
                for (Node node : seedNodes) {
                    addNode(node);
                }
            } else {
                NodeGroup outGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
                if (outGroup.size() < network.maxOutCount()) {
                    int size = network.maxOutCount() - handShakeNodes.size();
                    if (size > 0) {
                        getNodeFromDataBase(size);
                        getNodeFromOther(size);
                    }
                }
            }

            for (Node node : disConnectNodes.values()) {
                if (node.getType() == Node.OUT && node.getStatus() == Node.CLOSE) {
                    if (node.getLastFailTime() <= System.currentTimeMillis()) {
                        connectionManager.connectionNode(node);
                    }
                }
            }

            try {
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeSeedNode() {
        Collection<Node> nodes = connectedNodes.values();
        int count = 0;
        for (String ip : network.getSeedIpList()) {
            for (Node n : nodes) {
                if (n.getIp().equals(ip)) {
                    count++;
                    if (count > 2) {
                        removeNode(n);
                    }
                }
            }
        }
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

    public boolean isSeed() {
        return isSeed;
    }

    public void setSeed(boolean seed) {
        isSeed = seed;
    }
}
