package io.nuls.network.manager;

import io.netty.channel.socket.SocketChannel;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.connection.netty.NioChannelMap;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.network.storage.service.NetworkStorageService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class NodeManager implements Runnable {

    private static NodeManager instance = new NodeManager();

    private NodeManager() {
    }

    public static NodeManager getInstance() {
        return instance;
    }

    private NetworkParam networkParam = NetworkParam.getInstance();

    private Map<String, NodeGroup> nodeGroups = new ConcurrentHashMap<>();

    //存放未连接成功的节点
    private Map<String, Integer> firstUnConnectedNodes = new ConcurrentHashMap<>();
    //存放断开连接的节点
    private Map<String, Node> disConnectNodes = new ConcurrentHashMap<>();
    //存放连接成功但还未握手成功的节点
    private Map<String, Node> connectedNodes = new ConcurrentHashMap<>();
    //存放握手成功的节点
    private Map<String, Node> handShakeNodes = new ConcurrentHashMap<>();
    //存放所有正在连接或已连接的主动节点的id，防止重复连接
    private Set<String> outNodeIdSet = ConcurrentHashMap.newKeySet();

    private ReentrantLock lock = new ReentrantLock();

    private boolean running = false;

    private boolean isSeed = false;

    private ConnectionManager connectionManager;

    private NodeDiscoverHandler nodeDiscoverHandler;

    private NetworkStorageService networkStorageService;

    /**
     * 初始化主动连接节点组合(outGroup)被动连接节点组(inGroup)
     */
    public void init() {
        connectionManager = ConnectionManager.getInstance();
        nodeDiscoverHandler = NodeDiscoverHandler.getInstance();
        // init default NodeGroup
        NodeGroup inNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
        NodeGroup outNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
        nodeGroups.put(inNodes.getName(), inNodes);
        nodeGroups.put(outNodes.getName(), outNodes);

        for (String ip : IpUtil.getIps()) {
            if (isSeedNode(ip)) {
                networkParam.setMaxInCount(networkParam.getMaxInCount() * 2);
                isSeed = true;
            }
        }
    }

    /**
     * 启动的时候，从数据库里取出可用的节点和种子节点，尝试连接
     * 同时开启获取对方最新信息的线程
     */
    public void start() {
        List<Node> nodeList = getNetworkStorage().getLocalNodeList(20);
        nodeList.addAll(getSeedNodes());
        for (Node node : nodeList) {
            addNode(node);
        }
        running = true;
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "NetworkNodeManager", this);
        nodeDiscoverHandler.start();
    }

    /**
     * 重置网络节点
     */
    public void reset() {
        Log.debug("------------------network nodeManager reset--------------------");
        for (Node node : disConnectNodes.values()) {
            node.setFailCount(NetworkConstant.CONEECT_FAIL_MAX_COUNT);
        }
        for (Node node : handShakeNodes.values()) {
            removeNode(node);
        }
    }

    /**
     * 添加主动连接节点，并创建连接
     *
     * @param node
     * @return
     */
    public boolean addNode(Node node) {
        //判断是否是本地地址
        if (networkParam.getLocalIps().contains(node.getIp())) {
            return false;
        }
        lock.lock();
        try {
            //已连接的节点，不再重复连接
            if (outNodeIdSet.contains(node.getId())) {
                return false;
            }

            if (!checkFirstUnConnectedNode(node.getId())) {
                return false;
            }

            if (!disConnectNodes.containsKey(node.getId()) &&
                    !connectedNodes.containsKey(node.getId()) &&
                    !handShakeNodes.containsKey(node.getId())) {
                //判断是否有相同ip
                Map<String, Node> nodeMap = getNodes();
                for (Node n : nodeMap.values()) {
                    if (n.getIp().equals(node.getIp())) {
                        return false;
                    }
                }
                outNodeIdSet.add(node.getId());
                node.setType(Node.OUT);
                connectionManager.connectionNode(node);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 处理已经成功连接的节点
     *
     * @param node
     * @return
     */
    public boolean processConnectedNode(Node node) {
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

    /**
     * 添加节点到节点组
     *
     * @param groupName
     * @param node
     * @return
     */
    public boolean addNodeToGroup(String groupName, Node node) {
        NodeGroup nodeGroup = nodeGroups.get(groupName);
        if (nodeGroup == null) {
            //todo  throw new NulsExcetpion
//            throw new RuntimeException("group not found");
            return false;
        }
        if (groupName.equals(NetworkConstant.NETWORK_NODE_IN_GROUP) && nodeGroup.size() >= networkParam.getMaxInCount()) {
            return false;
        }
        if (groupName.equals(NetworkConstant.NETWORK_NODE_OUT_GROUP) && nodeGroup.size() >= networkParam.getMaxOutCount()) {
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

    /**
     * 获取当前所有节点（包括未连接成功和已连接的）
     *
     * @return
     */
    public Map<String, Node> getNodes() {
        Map<String, Node> nodeMap = new HashMap<>();
        nodeMap.putAll(disConnectNodes);
        nodeMap.putAll(connectedNodes);
        nodeMap.putAll(handShakeNodes);
        return nodeMap;
    }

    /**
     * 获取种子节点
     *
     * @return
     */
    public List<Node> getSeedNodes() {
        List<Node> seedNodes = new ArrayList<>();

        Set<String> localIp = IpUtil.getIps();
        for (String seedIp : networkParam.getSeedIpList()) {
            String[] ipPort = seedIp.split(":");
            if (!localIp.contains(ipPort[0])) {
                seedNodes.add(new Node(ipPort[0], Integer.parseInt(ipPort[1]), Integer.parseInt(ipPort[1]), Node.OUT));
            }
        }
        return seedNodes;
    }

    /**
     * 删除节点，如果发现节点组里没有此节点，说明一次都没有连接过，
     * 则直接删除数据库里的记录，不再继续尝试做连接
     *
     * @param nodeId
     */
    public void removeNode(String nodeId) {
        Node node = getNode(nodeId);
        if (node != null) {
            removeNode(node);
        } else {
//            Log.info("------------remove node is null-----------" + nodeId);
            getNetworkStorage().deleteNode(nodeId);
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
            getNetworkStorage().deleteNode(nodeId);
        }
    }

    /**
     * 删除节点分为主动删除和被动删除，当主动删除节点时，
     * 先需要关闭连接，之后再删除节点
     *
     * @param node
     */
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
                getNetworkStorage().deleteNode(node.getId());
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
            getNetworkStorage().deleteNode(node.getId());
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
        getNetworkStorage().saveNode(node);
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
        List<Node> nodes = getNetworkStorage().getLocalNodeList(size, ipSet);
        for (Node node : nodes) {
            addNode(node);
        }
    }

    private void getNodeFromOther(int size) {
        nodeDiscoverHandler.findOtherNode(size);
    }

    public boolean handshakeNode(String groupName, Node node, NetworkMessageBody versionMessage) {
        lock.lock();
        try {
            if (!checkFullHandShake(node)) {
                return false;
            }
            if (!connectedNodes.containsKey(node.getId())) {
                return false;
            }
            node.setStatus(Node.HANDSHAKE);
            node.setBestBlockHash(versionMessage.getBestBlockHash());
            node.setBestBlockHeight(versionMessage.getBestBlockHeight());

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
            return inGroup.size() < networkParam.getMaxInCount();
        } else {
            NodeGroup outGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
            return outGroup.size() < networkParam.getMaxOutCount();
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

    /**
     * 随机保留2个种子节点的连接，其他的全部断开
     */
    private void removeSeedNode() {
        Collection<Node> nodes = handShakeNodes.values();
        int count = 0;
        List<String> seedIpList = networkParam.getSeedIpList();
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
            for (String ip : networkParam.getSeedIpList()) {
                if (nodeId.startsWith(ip)) {
                    return;
                }
            }
            Integer count = firstUnConnectedNodes.get(nodeId);
            if (count == null) {
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
     *
     * @param nodeId
     * @return
     */
    private boolean checkFirstUnConnectedNode(String nodeId) {
        Integer count = firstUnConnectedNodes.get(nodeId);
        if (count == null)
            return true;
        if (count <= NetworkConstant.CONEECT_FAIL_MAX_COUNT) {
            // [0,6]
            return true;
        } else if (count < (NetworkConstant.CONEECT_FAIL_MAX_COUNT * 10)) {
            // (6, 60)
            firstUnConnectedNodes.put(nodeId, ++count);
            return false;
        } else {
            // [60, ~]
            firstUnConnectedNodes.remove(nodeId);
            return true;
        }
    }

    public boolean isSeedNode(String ip) {
        return networkParam.getSeedIpList().contains(ip);
    }

    public NodeGroup getNodeGroup(String groupName) {
        return nodeGroups.get(groupName);
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (running) {
            Log.info("--------disConnectNodes:" + disConnectNodes.size());
            for (Node node : disConnectNodes.values()) {
                System.out.println(node.toString());
            }

            Log.info("--------connectedNodes:" + connectedNodes.size());
            for (Node node : connectedNodes.values()) {
                System.out.println(node.toString());
            }

            Log.info("--------handShakeNodes:" + handShakeNodes.size());
            for (Node node : handShakeNodes.values()) {
                System.out.println(node.toString());
            }

            for (Node node : handShakeNodes.values()) {
                Log.info(node.toString() + ",blockHeight:" + node.getBestBlockHeight());
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.error(e);
            }

            //连接的节点数量太少时，主动连接种子节点以获取种子节点
            //超过一定数量之后，就断开与种子节点的连接，减轻种子节点的压力
            if (firstUnConnectedNodes.size() > 20) {
                firstUnConnectedNodes.clear();
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            if (handShakeNodes.size() <= 2) {
                List<Node> seedNodes = getSeedNodes();
                for (Node node : seedNodes) {
                    addNode(node);
                }
            } else if (handShakeNodes.size() > networkParam.getMaxOutCount()) {
                removeSeedNode();
            }
            NodeGroup outGroup = getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
            if (outGroup.size() < networkParam.getMaxOutCount()) {
                int size = networkParam.getMaxOutCount() - handShakeNodes.size();
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

    private NetworkStorageService getNetworkStorage() {
        if (networkStorageService == null) {
            networkStorageService = NulsContext.getServiceBean(NetworkStorageService.class);
        }
        return networkStorageService;
    }
}
