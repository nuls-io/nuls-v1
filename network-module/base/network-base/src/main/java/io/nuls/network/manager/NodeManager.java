package io.nuls.network.manager;

import io.netty.channel.socket.SocketChannel;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.connection.netty.NioChannelMap;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.storage.NetworkStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class NodeManager implements Runnable {

    private NetworkParam network = NetworkParam.getInstance();

    private Map<String, NodeGroup> nodeGroups = new ConcurrentHashMap<>();

    private Map<String, Integer> unConnectedNodes = new ConcurrentHashMap<>();

    private Map<String, Node> disConnectNodes = new ConcurrentHashMap<>();

    private Map<String, Node> connectedNodes = new ConcurrentHashMap<>();

    private Map<String, Node> handShakeNodes = new ConcurrentHashMap<>();

    private Set<String> outNodeIdSet = ConcurrentHashMap.newKeySet();

    private ReentrantLock lock = new ReentrantLock();

    private boolean running = false;

    @Autowired
    private NetworkStorage networkStorage;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private NodeDiscoverHandler nodeDiscoverHandler;

    public void init() {
        // init default NodeGroup
        NodeGroup inNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
        NodeGroup outNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
        nodeGroups.put(inNodes.getName(), inNodes);
        nodeGroups.put(outNodes.getName(), outNodes);
    }


    public void start() {
        List<Node> nodeList = networkStorage.getLocalNodeList(20);
        nodeList.addAll(getSeedNodes());
        for (Node node : nodeList) {
            addNode(node);
        }
        running = true;
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "NetworkNodeManager", this);
    }

    public void reset() {
        Log.debug("------------------network nodeManager reset--------------------");
        for (Node node : disConnectNodes.values()) {
            node.setFailCount(NetworkConstant.CONEECT_FAIL_MAX_COUNT);
        }
        for (Node node : handShakeNodes.values()) {
            removeNode(node);
        }
    }


    public boolean addNode(Node node) {
        if (network.getLocalIps().contains(node.getIp())) {
            return false;
        }
        lock.lock();
        try {

            if (outNodeIdSet.contains(node.getId())) {
                return false;
            }

            if (!checkFirstUnConnectedNode(node.getId())) {
                return false;
            }

            Map<String, Node> nodeMap = getNodes();
            for (Node n : nodeMap.values()) {
                if (n.getIp().equals(node.getIp())) {
                    return false;
                }
            }
            outNodeIdSet.add(node.getId());
            connectionManager.connectionNode(node);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean addConnNode(Node node) {
        lock.lock();
        try {
            if (!connectedNodes.containsKey(node.getId()) && !handShakeNodes.containsKey(node.getId())) {
                // those nodes that are not connected at once, remove it when connected +
                unConnectedNodes.remove(node.getId());
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


    public boolean addNodeToGroup(String groupName, Node node) {
        NodeGroup nodeGroup = nodeGroups.get(groupName);
        if (nodeGroup == null) {
            //todo  throw new NulsExcetpion
//            throw new RuntimeException("group not found");
            return false;
        }
        if (groupName.equals(NetworkConstant.NETWORK_NODE_IN_GROUP) && nodeGroup.size() >= network.getMaxInCount()) {
            return false;
        }
        if (groupName.equals(NetworkConstant.NETWORK_NODE_OUT_GROUP) && nodeGroup.size() >= network.getMaxOutCount()) {
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
        for (String seedIp : network.getSeedIpList()) {
            String[] ipPort = seedIp.split(":");
            if (!localIp.contains(ipPort[0])) {
                seedNodes.add(new Node(ipPort[0], Integer.parseInt(ipPort[1]), Integer.parseInt(ipPort[1]), Node.OUT));
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
            networkStorage.deleteNode(node);
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
            networkStorage.deleteNode(node);
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
                networkStorage.deleteNode(node);
            }
            return;
        }

        if (connectedNodes.containsKey(node.getId())) {
            connectedNodes.remove(node.getId());
        }
        if (handShakeNodes.containsKey(node.getId())) {
            handShakeNodes.remove(node.getId());
        }

        if (node.getFailCount() <= NetworkConstant.CONEECT_FAIL_MAX_COUNT) {
            //node.setLastFailTime(TimeService.currentTimeMillis() + 10 * 1000 * node.getFailCount());
            if (!disConnectNodes.containsKey(node.getId())) {
                disConnectNodes.put(node.getId(), node);
            }
        } else {
            disConnectNodes.remove(node.getId());
            networkStorage.deleteNode(node);
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
//        NodePo po = NodeTransferTool.toPojo(node);
//        getNodeDao().saveChange(po);
        networkStorage.saveNode(node);
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
        List<Node> nodes = networkStorage.getLocalNodeList(size, ipSet);
        for (Node node : nodes) {
            addNode(node);
        }
    }

    private void getNodeFromOther(int size) {
        nodeDiscoverHandler.findOtherNode(size);
    }


//    public boolean handshakeNode(String groupName, Node node, VersionEvent versionEvent) {
//        lock.lock();
//        try {
//            if (!checkFullHandShake(node)) {
//                return false;
//            }
//            if (!connectedNodes.containsKey(node.getId())) {
//                return false;
//            }
//            node.setStatus(Node.HANDSHAKE);
//            node.setVersionMessage(versionEvent);
//
//            connectedNodes.remove(node.getId());
//            handShakeNodes.put(node.getId(), node);
//            return addNodeToGroup(groupName, node);
//        } finally {
//            lock.unlock();
//        }
//    }

    private boolean checkFullHandShake(Node node) {
        if (node.getType() == Node.IN) {
            NodeGroup inGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
            return inGroup.size() < network.getMaxInCount();
        } else {
            NodeGroup outGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
            return outGroup.size() < network.getMaxOutCount();
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


    public NodeGroup getNodeGroup(String groupName) {
        return nodeGroups.get(groupName);
    }

    public void setNetwork(NetworkParam network) {
        this.network = network;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * those nodes that are not connected at once, reconnection 6 times
     *
     * @param nodeId
     * @return
     */
    public void validateFirstUnConnectedNode(String nodeId) {
        if (nodeId == null)
            return;
        Node node = getNode(nodeId);
        if (node == null) {
            // seed nodes
            for (String ip : network.getSeedIpList()) {
                if (nodeId.startsWith(ip)) {
                    return;
                }
            }
            Integer count = unConnectedNodes.get(nodeId);
            if (count == null) {
                unConnectedNodes.put(nodeId, 1);
            } else {
                unConnectedNodes.put(nodeId, ++count);
            }
        }
    }

    /**
     * those nodes that are not connected at once, reconnection 6 times
     * and then start counting, reconnection: counter >= 60
     * [0,6] (6, 60) [60,]
     *
     * @param nodeId
     * @return
     */
    private boolean checkFirstUnConnectedNode(String nodeId) {
        Integer count = unConnectedNodes.get(nodeId);
        if (count == null)
            return true;
        if (count <= NetworkConstant.CONEECT_FAIL_MAX_COUNT) {
            // [0,6]
            return true;
        } else if (count < (NetworkConstant.CONEECT_FAIL_MAX_COUNT * 10)) {
            // (6, 60)
            unConnectedNodes.put(nodeId, ++count);
            return false;
        } else {
            // [60, ~]
            unConnectedNodes.remove(nodeId);
            return true;
        }
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (running) {
            Log.info("disConnectNodes:" + disConnectNodes.size());
            Log.info("disConnectNodes:" + connectedNodes.size());
            Log.info("handShakeNodes:" + handShakeNodes.size());
            for (Node node : handShakeNodes.values()) {
             Log.info(node.toString() + ",blockHeight:" + node.getBestBlockHeight());
            }


        }

    }
}
