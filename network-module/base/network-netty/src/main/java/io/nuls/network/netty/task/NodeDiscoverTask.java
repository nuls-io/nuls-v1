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
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeConnectStatusEnum;
import io.nuls.network.model.NodeStatusEnum;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.netty.manager.ConnectionManager;
import io.nuls.network.netty.manager.NodeManager;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 节点发现任务
 * @author: ln
 * @date: 2018/12/8
 */
public class NodeDiscoverTask implements Runnable {

    private final NodeManager nodeManager = NodeManager.getInstance();
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();

    @Override
    public void run() {
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

                future.complete(new Object());

                node.setChannel(null);

                if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTED) {
                    node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
                    node.setStatus(NodeStatusEnum.CONNECTABLE);
                    canConnectNodes.put(node.getId(), node);

                    verifyNodes.remove(node.getId());
                } else if (nodeManager.getAvailableNodesCount() > 0) {

                    nodeManager.nodeConnectFail(node);

                    verifyNodes.remove(node.getId());
                }
            });

            boolean result = connectionManager.connection(node);
            if (!result) {
                verifyNodes.remove(node.getId());
            }
            try {
                future.get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
//                Log.error(e);
            }
        }
    }

}
