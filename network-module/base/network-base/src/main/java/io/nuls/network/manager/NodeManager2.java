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
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.storage.service.NetworkStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class NodeManager2 implements Runnable {

    private static NodeManager2 instance = new NodeManager2();

    private NodeManager2() {
    }

    public static NodeManager2 getInstance() {
        return instance;
    }

    private NetworkParam networkParam = NetworkParam.getInstance();

    private Map<String, NodeGroup> nodeGroups = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    private ConnectionManager connectionManager;

    private NodeDiscoverHandler nodeDiscoverHandler;

    private NetworkStorageService networkStorageService;

    boolean running;

    //存放所有正在连接或已连接的节点的id，防止重复连接
    private Set<String> nodeIdSet = ConcurrentHashMap.newKeySet();

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
//
//        for (String ip : IpUtil.getIps()) {
//            if (isSeedNode(ip)) {
//                networkParam.setMaxInCount(networkParam.getMaxInCount() * 2);
//                isSeed = true;
//            }
//        }
    }

    /**
     * 启动的时候，从数据库里取出所有的节点，尝试连接
     * 同时开启获取对方最新信息的线程
     */
    public void start() {
        //获取我自己的外网ip，防止自己连自己外网的情况出现
        String externalIp = getNetworkStorage().getExternalIp();
        if (externalIp != null) {
            networkParam.getLocalIps().add(externalIp);
        }

        List<Node> nodeList = getNetworkStorage().getLocalNodeList();
        nodeList.addAll(getSeedNodes());
        for (Node node : nodeList) {
            addNode(node);
        }
        running = true;
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "NetworkNodeManager", this);
        nodeDiscoverHandler.start();
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
            if (nodeIdSet.contains(node.getId())) {
                return false;
            }
            nodeIdSet.add(node.getId());
            node.setType(Node.OUT);
            connectionManager.connectionNode(node);
            return true;
        } finally {
            lock.unlock();
        }
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
     * 是否是种子节点
     *
     * @param ip
     * @return
     */
    public boolean isSeedNode(String ip) {
        for (String seedIp : networkParam.getSeedIpList()) {
            if (seedIp.indexOf(ip) != -1) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void run() {
        // todo auto-generated method stub

    }


    private NetworkStorageService getNetworkStorage() {
        if (networkStorageService == null) {
            networkStorageService = NulsContext.getServiceBean(NetworkStorageService.class);
        }
        return networkStorageService;
    }


}
