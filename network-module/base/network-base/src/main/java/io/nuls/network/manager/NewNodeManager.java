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
    //存放正常尝试连接或已连接成功的节点
    private Map<String, Node> connectedNodes = new ConcurrentHashMap<>();



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
     * 启动的时候，从数据库里取出所有的节点，尝试连接
     * 同时开启获取对方最新信息的线程
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
            for (Node node : nodeList) {
                nodeMap.put(node.getId(), node);
                if (node.getConnectStatus() == Node.SUCCESS) {
                    tryToConnect(node);
                }
            }
        } else {
            //如果为空，则视为第一启动，直接连接种子节点
            nodeDiscoverHandler.setFirstRunning(true);
            tryToConnectSeed();
        }
    }

    /**
     * 尝试连接
     *
     * @param node
     * @return
     */
    public boolean tryToConnect(Node node) {
        lock.lock();
        try {
            if (!connectValidate(node)) {
                return false;
            }
            connectedNodes.put(node.getId(), node);
            connectionManager.connectionNode(node);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 尝试连接种子节点
     */
    public void tryToConnectSeed() {
        lock.lock();
        try {
            for (Node node : seedNodes) {
                if (!connectValidate(node)) {
                    continue;
                }
                connectedNodes.put(node.getId(), node);
                connectionManager.connectionNode(node);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 随机保留2个种子节点的连接，其他的全部断开
     */
    void removeSeedNode() {
        List<Node> nodeList = new ArrayList<>(connectedNodes.values());
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
     * 主动连接验证规则
     * 1.本地地址不能连接
     * 2.存在相同ip的不能连接
     *
     * @param node
     * @return
     */
    private boolean connectValidate(Node node) {
        if (networkParam.getLocalIps().contains(node.getIp())) {
            return false;
        }
        for (Node connectNode : connectedNodes.values()) {
            if (connectNode.getIp().equals(node.getIp())) {
                return false;
            }
        }
        return true;
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


    /**
     *
     */
    @Override
    public void run() {

    }


    public Map<String, Node> getConnectedNodes() {
        return connectedNodes;
    }

    private NetworkStorageService getNetworkStorageService() {
        if (null == this.networkStorageService) {
            this.networkStorageService = NulsContext.getServiceBean(NetworkStorageService.class);
        }
        return this.networkStorageService;
    }
}
