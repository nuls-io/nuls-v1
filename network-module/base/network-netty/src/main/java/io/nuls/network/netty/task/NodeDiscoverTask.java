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
package io.nuls.network.netty.task;

import io.nuls.core.tools.log.Log;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.listener.EventListener;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeConnectStatusEnum;
import io.nuls.network.model.NodeStatusEnum;
import io.nuls.network.netty.broadcast.BroadcastHandler;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.netty.manager.ConnectionManager;
import io.nuls.network.netty.manager.NodeManager;
import io.nuls.network.protocol.message.P2PNodeBody;
import io.nuls.network.protocol.message.P2PNodeMessage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 节点发现任务
 * @author: ln
 * @date: 2018/12/8
 */
public class NodeDiscoverTask implements Runnable {

    private final NetworkParam networkParam = NetworkParam.getInstance();
    private final NodeManager nodeManager = NodeManager.getInstance();
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private final BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private boolean shareStatus;

    @Override
    public void run() {
        if(!shareStatus) {
            new Thread(() -> {
                try {
                    shareMyServer();
                } catch (Exception e) {
                    Log.error("share my server error", e);
                }
            }).start();
        }

        processNodes();
    }

    private void processNodes() {
        NodesContainer nodesContainer = nodeManager.getNodesContainer();

        Map<String, Node> canConnectNodes = nodesContainer.getCanConnectNodes();

        Map<String, Node> uncheckNodes = nodesContainer.getUncheckNodes();
        Map<String, Node> failNodes = nodesContainer.getFailNodes();
        Map<String, Node> disconnectNodes = nodesContainer.getDisconnectNodes();

        if (uncheckNodes.size() > 0) {
            probeNodes(uncheckNodes, canConnectNodes);
        }

        if (failNodes.size() > 0) {
            probeNodes(failNodes, canConnectNodes);
        }

        if (disconnectNodes.size() > 0) {
            probeNodes(disconnectNodes, canConnectNodes);
        }
    }

    private void probeNodes(Map<String, Node> verifyNodes, Map<String, Node> canConnectNodes) {

        for (Map.Entry<String, Node> nodeEntry : verifyNodes.entrySet()) {

            CompletableFuture future = new CompletableFuture<>();

            Node node = nodeEntry.getValue();

            node.setConnectedListener(() -> {
                node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
                node.getChannel().close();
            });

            node.setDisconnectListener(() -> {
                node.setChannel(null);

                if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTED) {
                    node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
                    node.setStatus(NodeStatusEnum.CONNECTABLE);
                    canConnectNodes.put(node.getId(), node);

                    verifyNodes.remove(node.getId());
                } else if (nodeManager.getAvailableNodesCount() > 0) {
                    verifyNodes.remove(node.getId());
                }
                future.complete(new Object());
            });

            boolean result = connectionManager.connection(node);
            if (!result) {
                verifyNodes.remove(node.getId());
            }
            try {
                future.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
            }
        }
    }


    private void shareMyServer() {

        if(shareStatus) {
            return;
        }
        shareStatus = true;

        String externalIp = getMyExtranetIp();

        if (externalIp == null) {
            shareStatus = false;
            return;
        }

        Log.info("my external ip  is {}" , externalIp);

        networkParam.getLocalIps().add(externalIp);

        Node myNode = new Node(externalIp, networkParam.getPort(), Node.OUT);

        myNode.setConnectedListener(() -> {

            Log.info("============ connect myself success ========");

            myNode.getChannel().close();
            doShare(externalIp);
        });

        myNode.setDisconnectListener(new EventListener() {
            @Override
            public void action() {

                Log.info("============ disconnect myself ========");

                myNode.setChannel(null);
            }
        });

        boolean success = connectionManager.connection(myNode);
        Log.info("try connect myself {} ", success);
    }

    private String getMyExtranetIp() {
        int nodeCount = 0;
        long timeout = 20000L;
        long lastTime = System.currentTimeMillis();

        while (true) {
            int count = nodeManager.getAvailableNodesCount();
            if (count == nodeCount && count >= 1) {
                if (System.currentTimeMillis() - lastTime > timeout) {
                    break;
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            } else {
                nodeCount = count;
                lastTime = System.currentTimeMillis();
            }
        }

        Collection<Node> nodes = nodeManager.getAvailableNodes();

        return getMostSameIp(nodes);
    }

    private String getMostSameIp(Collection<Node> nodes) {

        Map<String, Integer> ipMaps = new HashMap<>();

        for (Node node : nodes) {
            String ip = node.getExternalIp();
            if (ip == null) {
                continue;
            }
            Integer count = ipMaps.get(ip);
            if (count == null) {
                ipMaps.put(ip, 1);
            } else {
                ipMaps.put(ip, count + 1);
            }
        }

        int maxCount = 0;
        String ip = null;
        for (Map.Entry<String, Integer> entry : ipMaps.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                ip = entry.getKey();
            }
        }

        return ip;
    }

    private void doShare(String externalIp) {
        P2PNodeBody p2PNodeBody = new P2PNodeBody(externalIp, networkParam.getPort());
        P2PNodeMessage message = new P2PNodeMessage(p2PNodeBody);
        broadcastHandler.broadcastToAllNode(message, null, true, 100);
    }
}
