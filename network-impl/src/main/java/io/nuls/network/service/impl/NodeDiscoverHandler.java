/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.service.impl;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.db.dao.NodeDataService;
import io.nuls.db.entity.NodePo;
import io.nuls.network.NetworkContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.NodeTransferTool;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.entity.GetNodeEvent;
import io.nuls.network.message.entity.GetNodesIpEvent;
import io.nuls.network.message.entity.GetVersionEvent;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NodeDiscoverHandler implements Runnable {

    private AbstractNetworkParam network;

    private NodesManager nodesManager;

    private NodeDataService nodeDao;

    private BroadcastHandler broadcaster;

    private boolean running;

    private NodeDiscoverHandler() {

    }

    private static NodeDiscoverHandler instance = new NodeDiscoverHandler();

    public static NodeDiscoverHandler getInstance() {
        return instance;
    }

    public void start() {
        running = true;
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_NETWORK, "NetworkNodeDiscover", this);
    }

    // get nodes from local database
    public List<Node> getLocalNodes(int size) {
        Set<String> ipList = new HashSet<>();
        for (Node node : nodesManager.getNodes().values()) {
            ipList.add(node.getIp());
        }
        List<NodePo> nodePos = getNodeDao().getNodePoList(size, ipList);

        List<Node> nodes = new ArrayList<>();
        if (nodePos == null || nodePos.isEmpty()) {
            return nodes;
        }
        for (NodePo po : nodePos) {
            Node node = new Node();
            NodeTransferTool.toNode(node, po);
            node.setType(Node.OUT);
            node.setStatus(Node.WAIT);
            node.setMagicNumber(network.packetMagic());
            nodes.add(node);
        }
        return nodes;
    }


    public List<Node> getSeedNodes() {
        List<Node> seedNodes = new ArrayList<>();
        for (InetSocketAddress socketAddress : network.getSeedNodes()) {
            // remove myself
            if (network.getLocalIps().contains(socketAddress.getHostString())) {
                continue;
            }
            seedNodes.add(new Node(network.packetMagic(), Node.OUT, socketAddress));
        }
        return seedNodes;
    }

    /**
     * Inquire more of the other nodes to the connected nodes
     *
     * @param size
     */
    public void findOtherNode(int size) {
        GetNodeEvent event = new GetNodeEvent(size);

        NodeGroup group = nodesManager.getNodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
        if (group.getNodes().size() > 0) {
            List<Node> nodeList = new ArrayList<>(group.getNodes().values());
            Collections.shuffle(nodeList);
            for (Node node : nodeList) {
                if (node.isHandShake()) {
                    broadcaster.broadcastToNode(event, node, true);
                    break;
                }
            }
        }

        group = nodesManager.getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
        if (group.getNodes().size() > 0) {
            List<Node> nodeList = new ArrayList<>(group.getNodes().values());
            Collections.shuffle(nodeList);
            for (Node node : nodeList) {
                if (node.isHandShake()) {
                    broadcaster.broadcastToNode(event, node, true);
                    break;
                }
            }
        }
    }

    private static int count = 0;

    /**
     * do ping/pong and ask versionMessage
     */
    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        while (running) {
            count++;
            List<Node> nodeList = new ArrayList<>(nodesManager.getNodes().values());
            StringBuilder str = new StringBuilder();
            int count = 0;
            for (Node node : nodeList) {
                if (node.getStatus() == 2) {
                    str.append(",");
                    str.append(node.getIp());
                    count++;
                }
            }
            if (str.toString().length() == 0) {
                str.append(",");
            }
            Log.info("nodes(" + count + "):" + str.toString().substring(1));

            GetVersionEvent event = new GetVersionEvent(network.port());
            GetNodesIpEvent ipEvent = new GetNodesIpEvent();
            for (Node node : nodeList) {
                if (node.isAlive()) {
                    broadcaster.broadcastToNode(event, node, true);
                    if (count == 10) {
                        broadcaster.broadcastToNode(ipEvent, node, true);
                    }
                }
            }

            long now = TimeService.currentTimeMillis();

            if (count == 10) {
                count = 0;
                List<String> list = new ArrayList<>();
                for (Map.Entry<String, Long> entry : NetworkContext.ipMap.entrySet()) {
                    if (now - entry.getValue() > DateUtil.MINUTE_TIME * 2) {
                        list.add(entry.getKey());
                    }
                }
                for (String ip : list) {
                    NetworkContext.ipMap.remove(ip);
                }
            }

            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
    }

    public void setNetwork(AbstractNetworkParam network) {
        this.network = network;
    }

    public void setNodesManager(NodesManager nodesManager) {
        this.nodesManager = nodesManager;
    }

    public void setBroadcaster(BroadcastHandler broadcaster) {
        this.broadcaster = broadcaster;
    }

    private NodeDataService getNodeDao() {
        if (nodeDao == null) {
            nodeDao = NulsContext.getServiceBean(NodeDataService.class);
        }
        return nodeDao;
    }
}
