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

import io.netty.channel.socket.SocketChannel;
import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.network.IpUtil;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.db.dao.NodeDataService;
import io.nuls.db.entity.NodePo;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.NodeTransferTool;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.entity.VersionEvent;
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

    private Map<String, Integer> firstUnConnectedNodes = new ConcurrentHashMap<>();

    private Map<String, Node> disConnectNodes = new ConcurrentHashMap<>();

    private Map<String, Node> connectedNodes = new ConcurrentHashMap<>();

    private Map<String, Node> handShakeNodes = new ConcurrentHashMap<>();

    private Set<String> outNodeIdSet = ConcurrentHashMap.newKeySet();

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
        List<Node> nodeList = discoverHandler.getLocalNodes(20, null);
        nodeList.addAll(getSeedNodes());
        for (Node node : nodeList) {
            addNode(node);
        }
        running = true;
        discoverHandler.start();
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_NETWORK, "NetworkNodeManager", this);
    }

    public void reset() {
        Log.info("------------------network nodeManager reset--------------------");
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
            if (outNodeIdSet.contains(node.getId())) {
                return false;
            }

            if(!checkFirstUnConnectedNode(node.getId())) {
                return false;
            }

            if (!disConnectNodes.containsKey(node.getId()) &&
                    !connectedNodes.containsKey(node.getId()) &&
                    !handShakeNodes.containsKey(node.getId())) {
                Map<String, Node> nodeMap = getNodes();
                for (Node n : nodeMap.values()) {
                    if (n.getIp().equals(node.getIp())) {
                        return false;
                    }
                }
                outNodeIdSet.add(node.getId());
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
                // those nodes that are not connected at once, remove it when connected +
                firstUnConnectedNodes.remove(node.getId());
                // those nodes that are not connected at once, remove it when connected -
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

    public Node getHandshakeNode(String nodeId) {
        Node node = handShakeNodes.get(nodeId);
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
//            Log.info("------------remove node is null-----------" + nodeId);
            getNodeDao().removeNode(nodeId);
            outNodeIdSet.remove(nodeId);
        }
    }

    public void removeHandshakeNode(String nodeId) {
        Node node = getHandshakeNode(nodeId);
        if (node != null) {
            removeNode(node);
        } else {
//            Log.info("------------removeHandshakeNode node is null-----------" + nodeId);
            outNodeIdSet.remove(node.getId());
            getNodeDao().removeNode(nodeId);
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
            outNodeIdSet.remove(node.getId());
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
                getNodeDao().removeNode(node.getPoId());
            }
            return;
        }

        if (connectedNodes.containsKey(node.getId())) {
            connectedNodes.remove(node.getId());
        }
        if (handShakeNodes.containsKey(node.getId())) {
            handShakeNodes.remove(node.getId());
        }

        if (node.getFailCount() <= NetworkConstant.FAIL_MAX_COUNT) {
            //node.setLastFailTime(TimeService.currentTimeMillis() + 10 * 1000 * node.getFailCount());
            if (!disConnectNodes.containsKey(node.getId())) {
                disConnectNodes.put(node.getId(), node);
            }
        } else {
            disConnectNodes.remove(node.getId());
            getNodeDao().removeNode(node.getPoId());
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

    public void saveNode(Node node) {
        NodePo po = NodeTransferTool.toPojo(node);
        getNodeDao().saveChange(po);
    }

    public void deleteNode(String nodeId) {
        outNodeIdSet.remove(nodeId);
        disConnectNodes.remove(nodeId);
    }

    private void removeNodeFromGroup(Node node, String groupName) {
        NodeGroup group = nodeGroups.get(groupName);
        if (group != null) {
            group.removeNode(node.getId());
        }
        node.getGroupSet().remove(groupName);
    }


    private void getNodeFromDataBase(int size) {
        Set<String> ipSet = new HashSet<>();
        for (Node node : getNodes().values()) {
            ipSet.add(node.getIp());
        }
        List<Node> nodes = discoverHandler.getLocalNodes(size, ipSet);
        for (Node node : nodes) {
            addNode(node);
        }
    }

    private void getNodeFromOther(int size) {
        discoverHandler.findOtherNode(size);
    }


    public boolean handshakeNode(String groupName, Node node, VersionEvent versionEvent) {
        lock.lock();
        try {
            if (!checkFullHandShake(node)) {
                return false;
            }
            if (!connectedNodes.containsKey(node.getId())) {
                return false;
            }
            node.setStatus(Node.HANDSHAKE);
            node.setVersionMessage(versionEvent);

            connectedNodes.remove(node.getId());
            handShakeNodes.put(node.getId(), node);
            return addNodeToGroup(groupName, node);
        } finally {
            lock.unlock();
        }
    }

    private boolean checkFullHandShake(Node node) {
        if (node.getType() == Node.IN) {
            NodeGroup inGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
            return inGroup.size() < network.maxInCount();
        } else {
            NodeGroup outGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
            return outGroup.size() < network.maxOutCount();
        }
    }


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

    private void removeSeedNode() {
        Collection<Node> nodes = handShakeNodes.values();
        int count = 0;
        List<String> seedIpList = network.getSeedIpList();
        Collections.shuffle(seedIpList);

        for (Node n : nodes) {
            if (seedIpList.contains(n.getIp())) {
                count++;
                if (count > 2) {
                    removeNode(n);
                }
            }
        }
    }

    public boolean isSeedNode(String ip) {
        return network.getSeedIpList().contains(ip);
    }

    /**
     * check the nodes when closed try to connect other one
     */
    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (running) {
//            Log.info("disConnectNodes:" + disConnectNodes.size());
//            Log.info("disConnectNodes:" + connectedNodes.size());
//            Log.info("handShakeNodes:" + handShakeNodes.size());
            //for (Node node : handShakeNodes.values()) {
               // Log.info(node.toString() + ",blockHeight:" + node.getVersionMessage().getBestBlockHeight());
            //}

            if(firstUnConnectedNodes.size() > 20) {
                firstUnConnectedNodes.clear();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (handShakeNodes.size() <= 2) {
                List<Node> seedNodes = getSeedNodes();
                for (Node node : seedNodes) {
                    addNode(node);
                }
            } else if (handShakeNodes.size() > network.maxOutCount()) {
                removeSeedNode();
            }
            NodeGroup outGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
            if (outGroup.size() < network.maxOutCount()) {
                int size = network.maxOutCount() - handShakeNodes.size();
                if (size > 0) {
                    getNodeFromDataBase(size);
                    getNodeFromOther(size);
                }
            }


            for (Node node : disConnectNodes.values()) {
                if (node.getType() == Node.OUT && node.getStatus() == Node.CLOSE) {
                    /*if (node.getLastFailTime() <= TimeService.currentTimeMillis()) {
                        connectionManager.connectionNode(node);
                    }*/
                    connectionManager.connectionNode(node);
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

    /**
     * those nodes that are not connected at once, reconnection 6 times
     * @param nodeId
     * @return
     */
    public void validateFirstUnConnectedNode(String nodeId) {
        if(nodeId == null)
            return;
        Node node = getNode(nodeId);
        if(node == null) {
            // seed nodes
            for (String ip : network.getSeedIpList()) {
                if(nodeId.startsWith(ip)) {
                    return;
                }
            }
            Integer count = firstUnConnectedNodes.get(nodeId);
            if(count == null) {
                firstUnConnectedNodes.put(nodeId, 1);
            } else {
                firstUnConnectedNodes.put(nodeId, ++count);
            }
        }
    }

    /**
     * those nodes that are not connected at once, reconnection 6 times
     * and then start counting, reconnection: counter >= 60
     * [0,6] (6, 60) [60,]
     * @param nodeId
     * @return
     */
    private boolean checkFirstUnConnectedNode(String nodeId) {
        Integer count = firstUnConnectedNodes.get(nodeId);
        if(count == null)
            return true;
        if(count <= NetworkConstant.FAIL_MAX_COUNT) {
            // [0,6]
            return true;
        } else if(count < (NetworkConstant.FAIL_MAX_COUNT * 10)){
            // (6, 60)
            firstUnConnectedNodes.put(nodeId, ++count);
            return false;
        } else {
            // [60, ~]
            firstUnConnectedNodes.remove(nodeId);
            return true;
        }
    }
}
