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

import io.netty.channel.socket.SocketChannel;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.network.IpUtil;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.connection.netty.NioChannelMap;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.storage.service.NetworkStorageService;

import java.util.*;
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
    //存放断开连接的节点
    private Map<String, Node> disConnectNodes = new ConcurrentHashMap<>();
    //存放连接成功但还未握手成功的节点
    private Map<String, Node> connectedNodes = new ConcurrentHashMap<>();
    //存放握手成功的节点
    private Map<String, Node> handShakeNodes = new ConcurrentHashMap<>();

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
            disConnectNodes.put(node.getId(), node);
            connectionManager.connectionNode(node);
            return true;
        } finally {
            lock.unlock();
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

            node.destroy();
            removeNodeFromGroup(node);
            removeNodeHandler(node);
            nodeIdSet.remove(node.getId());
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

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        while (running) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.debug("--------disConnectNodes:" + disConnectNodes.size());
            for (Node node : disConnectNodes.values()) {
                System.out.println(node.toString());
            }

            Log.debug("--------connectedNodes:" + connectedNodes.size());
            for (Node node : connectedNodes.values()) {
                System.out.println(node.toString());
            }

            Log.debug("--------handShakeNodes:" + handShakeNodes.size());
            for (Node node : handShakeNodes.values()) {
                Log.debug(node.toString() + ",blockHeight:" + node.getBestBlockHeight());
            }

            if (handShakeNodes.size() > networkParam.getMaxOutCount()) {
                removeSeedNode();
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
