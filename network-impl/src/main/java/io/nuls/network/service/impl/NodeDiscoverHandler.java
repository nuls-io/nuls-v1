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

import io.nuls.db.dao.NodeDataService;
import io.nuls.db.entity.NodePo;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.NodeTransfer;
import io.nuls.network.entity.param.AbstractNetworkParam;

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

    private boolean running;


    public NodeDiscoverHandler(NodesManager nodesManager, AbstractNetworkParam network, NodeDataService nodeDao) {
        this.nodesManager = nodesManager;
        this.network = network;
        this.running = true;
        this.nodeDao = nodeDao;
    }

    // get nodes from local database
    public List<Node> getLocalNodes(int size) {
        Set<String> keys = nodesManager.getNodes().keySet();
        List<NodePo> nodePos = nodeDao.getRandomNodePoList(size, keys);

        List<Node> nodes = new ArrayList<>();
        if (nodePos == null || nodePos.isEmpty()) {
            return nodes;
        }
        for (NodePo po : nodePos) {
            Node node = new Node(network);
            NodeTransfer.toNode(node, po);
            nodes.add(node);
        }
        return nodes;
    }


    public List<Node> getSeedNodes() {
        List<Node> seedNodes = new ArrayList<>();
        for (InetSocketAddress socketAddress : network.getSeedNodes()) {
            // remove myself
//todo            if (network.getLocalIps().contains(socketAddress.getAddress().getHostAddress())) {
//                continue;
//            }
            seedNodes.add(new Node(network, Node.OUT, socketAddress));
        }
        return seedNodes;
    }

    /**
     * check the nodes when closed try to connect other one
     */
    @Override
    public void run() {
        while (running) {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            for (Node node : nodesManager.getNodes().values()) {
                if (node.getStatus() == Node.CLOSE) {
                    nodesManager.removeNode(node.getHash());
                }
            }

            NodeGroup outNodes = nodesManager.getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
            if (outNodes.size() == 0) {
                //  The seedNodes should be connected immediately
                List<Node> nodes = getSeedNodes();

                for (Node newNode : nodes) {
                    if (outNodes.getNodes().values().contains(newNode)) {
                        continue;
                    }
                    nodesManager.addNodeToGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP, newNode);
                }
            } else if (outNodes.size() < network.maxOutCount()) {
                List<Node> nodes = getLocalNodes(network.maxOutCount() - outNodes.size());
                if (nodes.isEmpty()) {
//                    // find other node from connected nodes
                    findOtherNode(network.maxOutCount() - outNodes.size());
                } else {
                    for (Node newNode : nodes) {
                        if (outNodes.getNodes().values().contains(newNode)) {
                            continue;
                        }
                        nodesManager.addNodeToGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP, newNode);
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Inquire more of the other nodes to the connected nodes
     *
     * @param size
     */
    private void findOtherNode(int size) {
//        NodeGroup group = nodesManager.getNodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP);
//        if (group.getNodes().size() > 0) {
//            Node node = group.getNodes().get(0);
//            if (node.isHandShake()) {
//                try {
//                    GetNodeEvent data = new GetNodeEvent(size);
//                    node.sendNetworkData(data);
//                } catch (Exception e) {
//                    Log.warn("send getNodeData error", e);
//                    node.destroy();
//                }
//            }
//        }
//
//        group = nodesManager.getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP);
//        if (group.getNodes().size() > 0) {
//            Node node = group.getNodes().get(0);
//            if (node.isHandShake()) {
//                try {
//                    GetNodeEvent data = new GetNodeEvent(size);
//                    node.sendNetworkData(data);
//                } catch (Exception e) {
//                    Log.warn("send getNodeData error", e);
//                    node.destroy();
//                }
//            }
//        }
    }
}
