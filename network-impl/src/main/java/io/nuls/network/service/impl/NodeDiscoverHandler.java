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

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.db.dao.NodeDataService;
import io.nuls.db.entity.NodePo;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.NodeTransferTool;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.entity.GetNodeEvent;
import io.nuls.network.message.entity.GetVersionEvent;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        Set<String> keys = nodesManager.getNodes().keySet();
        List<NodePo> nodePos = getNodeDao().getNodePoList(size, keys);

        List<Node> nodes = new ArrayList<>();
        if (nodePos == null || nodePos.isEmpty()) {
            return nodes;
        }
        for (NodePo po : nodePos) {
            Node node = new Node(network);
            NodeTransferTool.toNode(node, po);
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
            seedNodes.add(new Node(network, Node.OUT, socketAddress));
        }
        return seedNodes;
    }

    /**
     * Inquire more of the other nodes to the connected nodes
     *
     * @param size
     */
    public void findOtherNode(int size) {
        NodeGroup group = nodesManager.getNodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
        if (group.getNodes().size() > 0) {
            List<Node> nodeList = new ArrayList<>(group.getNodes().values());
            for (Node node : nodeList) {
                if (node.isHandShake()) {
                    GetNodeEvent event = new GetNodeEvent(size);
                    broadcaster.broadcastToNode(event, node, true);
                    break;
                }
            }
        }

        group = nodesManager.getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
        if (group.getNodes().size() > 0) {
            List<Node> nodeList = new ArrayList<>(group.getNodes().values());
            for (Node node : nodeList) {
                if (node.isHandShake()) {
                    GetNodeEvent event = new GetNodeEvent(size);
                    broadcaster.broadcastToNode(event, node, true);
                    break;
                }
            }
        }
    }

    /**
     * do ping/pong and ask versionMessage
     */
    @Override
    public void run() {
        while (running) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            List<Node> nodeList = new ArrayList<>(nodesManager.getNodes().values());

            for (Node node : nodeList) {
                if (node.isAlive()) {
                    GetVersionEvent event = new GetVersionEvent(network.getExternalPort());
                    broadcaster.broadcastToNode(event, node, true);
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
