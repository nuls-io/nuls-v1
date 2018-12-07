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

package io.nuls.network.manager;

import io.netty.channel.Channel;
import io.nuls.core.tools.date.DateUtil;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.kernel.context.NulsContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.storage.service.NetworkStorageService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 网络节点管理器
 * 创建节点池存放所有节点
 * 通过P2PNodeMessage获取到的节点，连接探测状态都标记为uncheck，放入到节点池类
 * 种子节点或者是从其他已连接节点获取到的更多节点，连接探测状态都标记为failed，放入到节点池类
 * 定期尝试连接池里的所有非握手成功状态的节点。连接成功的标记为success，若之前状态为uncheck的连接成功后，将改节点广播给其他节点，
 * 连接失败的记录失败次数，失败次数过大，则从连接池里移除
 */
public class NewNodeManager implements Runnable {

    private static NewNodeManager instance = new NewNodeManager();

    private NewNodeManager() {

    }

    public static NewNodeManager getInstance() {
        return instance;
    }

    private NetworkParam networkParam = NetworkParam.getInstance();

    private Map<String, NodeGroup> nodeGroups = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    private ConnectionManager connectionManager;

    private NewNodeDiscoverHandler nodeDiscoverHandler;

    private NetworkStorageService networkStorageService;

    private BroadcastHandler broadcastHandler;

    boolean running;

    //是否已尝试过自己连接自己
    volatile boolean connectedMySelf;
    //当前节点是否是种子节点
    volatile boolean isSeed;
    //存放种子节点
    private List<Node> seedNodes;

    //节点池，存放所有节点
    private Map<String, Node> nodeMap = new ConcurrentHashMap<>();
    //已握手成功的连接池，存放已握手成功的主动连接和被动连接的节点，方便广播消息
    private Map<String, Node> handerShakeNode = new ConcurrentHashMap<>();

    /**
     * 初始化主动连接节点组合(outGroup)被动连接节点组(inGroup)
     */
    public void init() {
        connectionManager = ConnectionManager.getInstance();
        nodeDiscoverHandler = NewNodeDiscoverHandler.getInstance();
        broadcastHandler = BroadcastHandler.getInstance();
        // init default NodeGroup
        NodeGroup inNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
        NodeGroup outNodes = new NodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
        nodeGroups.put(inNodes.getName(), inNodes);
        nodeGroups.put(outNodes.getName(), outNodes);

        seedNodes = nodeDiscoverHandler.getSeedNodes();
        //判断自己是否是种子节点
        for (String ip : IpUtil.getIps()) {
            if (isSeedNode(ip)) {
                isSeed = true;
            }
        }
    }

    /**
     * 启动的时候，从数据库里取出所有的节点，放入节点池中
     * 并立即尝试连接那些标记为可连接的节点，
     * 如果是第一次启动则直接连接种子节点
     */
    public void start() {
        //获取我自己的外网ip，防止自己连自己外网的情况出现
        String externalIp = getNetworkStorageService().getExternalIp();
        if (externalIp != null) {
            networkParam.getLocalIps().add(externalIp);
        }

        //获取数据库存储的节点信息尝试连接
        List<Node> nodeList = getNetworkStorageService().getLocalNodeList();
        if (nodeList != null && nodeList.isEmpty()) {
            //如果为空，则视为第一启动，直接连接种子节点
            nodeDiscoverHandler.setFirstRunning(true);
            nodeList = seedNodes;
        }
        for (Node node : nodeList) {
            nodeMap.put(node.getIp(), node);
            if (node.getConnectStatus() == Node.SUCCESS) {
                tryToConnect(node);
            }
        }
    }

    /**
     * 尝试主动连接
     *
     * @param node
     * @return
     */
    public boolean tryToConnect(Node node) {
        lock.lock();
        try {
            //如果节点已经处于连接状态，不要重复发送连接
            if (node.isAlive()) {
                return false;
            }
            connectionManager.connectionNode(node);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 尝试连接种子节点
     */
    public void connectSeedNode() {
        for (Node node : seedNodes) {
            if (!nodeMap.containsKey(node.getIp())) {
                nodeMap.put(node.getIp(), node);
            }
            tryToConnect(node);
        }
    }

    /**
     * 随机保留2个种子节点的连接，其他的种子节点都断开
     */
    void removeSeedNode() {
        List<Node> nodeList = new ArrayList<>(handerShakeNode.values());
        int count = 0;
        List<String> seedIpList = networkParam.getSeedIpList();
        Collections.shuffle(nodeList);

        for (Node n : nodeList) {
            if (seedIpList.contains(n.getIp())) {
                count++;
                if (count > 2) {
                    //  removeNode(n);
                }
            }
        }
    }

    /**
     * 从池子里查找count个可连接的节点尝试做连接
     *
     * @param count
     */
    void findNodeAndConnect(int count) {

        int successCount = 0;
        for (Node node : nodeMap.values()) {
            if (node.getConnectStatus() == Node.SUCCESS && !node.isAlive()) {
                if (tryToConnect(node)) {
                    successCount++;
                }
                if (successCount == count) {
                    break;
                }
            }
        }
    }

    /**
     * 添加新的节点到节点池
     *
     * @param node
     * @return
     */
    public boolean addNode(Node node) {
        if (!validateAddNode(node)) {
            return false;
        }
        nodeMap.put(node.getIp(), node);
        return true;
    }

    /**
     * 添加主动节点规则
     * 1.本地地址不能添加
     * 2.存在相同ip的不能添加
     *
     * @param node
     * @return
     */
    private boolean validateAddNode(Node node) {
        if (networkParam.getLocalIps().contains(node.getIp())) {
            return false;
        }
        for (Node connectNode : nodeMap.values()) {
            if (connectNode.getIp().equals(node.getIp())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 处理已经成功连接的节点
     */
    public boolean processConnectedNode(Node node, Channel channel) {
        lock.lock();
        try {
            if (handerShakeNode.containsKey(node.getId())) {
                return false;
            }
            node.setChannel(channel);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 是否是种子节点
     */
    public boolean isSeedNode(String ip) {
        for (Node node : seedNodes) {
            if (node.getIp().equals(ip)) {
                return true;
            }
        }
        return false;
    }


    public Node getNode(String ip) {
        return nodeMap.get(ip);
    }

    /**
     * 获取已连接握手成功的节点
     *
     * @return
     */
    public List<Node> getAvailableNodes() {
        List<Node> nodeList = new ArrayList<>();
        for (Node node : handerShakeNode.values()) {
            if (node.isHandShake()) {
                nodeList.add(node);
            }
        }
        return nodeList;
    }

    /**
     * 获取可连接的节点
     *
     * @return
     */
    public List<Node> getCanConnectNodes() {
        List<Node> nodeList = new ArrayList<>();
        for (Node node : nodeMap.values()) {
            if (node.getConnectStatus() == Node.SUCCESS) {
                nodeList.add(node);
            }
        }
        return nodeList;
    }


    /**
     * 定期尝试连接节点池中处于未连接状态的节点，更新节点状态
     */
    @Override
    public void run() {
        while (true) {
            for (Node node : nodeMap.values()) {
                tryToConnect(node);
            }
            //TODO 这里考虑，失败的节点和uncheck的节点采用不同的时间去尝试连接
            try {
                Thread.sleep(60 * DateUtil.MINUTE_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private NetworkStorageService getNetworkStorageService() {
        if (null == this.networkStorageService) {
            this.networkStorageService = NulsContext.getServiceBean(NetworkStorageService.class);
        }
        return this.networkStorageService;
    }

    public Map<String, Node> getHanderShakeNode() {
        return handerShakeNode;
    }
}
