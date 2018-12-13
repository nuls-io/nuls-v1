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
import io.nuls.kernel.func.TimeService;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeConnectStatusEnum;
import io.nuls.network.model.NodeStatusEnum;
import io.nuls.network.netty.broadcast.BroadcastHandler;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.netty.manager.ConnectionManager;
import io.nuls.network.netty.manager.NodeManager;
import io.nuls.network.protocol.message.P2PNodeBody;
import io.nuls.network.protocol.message.P2PNodeMessage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 节点发现任务
 * @author: ln
 * @date: 2018/12/8
 */
public class NodeDiscoverTask implements Runnable {

    // 节点探测结果 -- 成功，能连接
    private final static int PROBE_STATUS_SUCCESS = 1;
    // 节点探测结果 -- 失败，不能连接，节点不可用
    private final static int PROBE_STATUS_FAIL = 2;
    // 节点探测结果 -- 忽略，当断网时，也就是本地节点一个都没有连接时，不确定是对方连不上，还是本地没网，这时忽略
    private final static int PROBE_STATUS_IGNORE = 3;

    private final NodeManager nodeManager = NodeManager.getInstance();
    private final BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();

    private boolean isProcessFailNodes;

    public NodeDiscoverTask(boolean isProcessFailNodes) {
        this.isProcessFailNodes = isProcessFailNodes;
    }

    @Override
    public void run() {
        try {
            processNodes();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void processNodes() {
        NodesContainer nodesContainer = nodeManager.getNodesContainer();

        Map<String, Node> canConnectNodes = nodesContainer.getCanConnectNodes();

        if (isProcessFailNodes) {
            Map<String, Node> failNodes = nodesContainer.getFailNodes();

//        Log.info("the fail nodes count is {}", failNodes.size());

            if (failNodes.size() > 0) {
                probeNodes(failNodes, canConnectNodes);
            }
        } else {
            Map<String, Node> uncheckNodes = nodesContainer.getUncheckNodes();
            Map<String, Node> disconnectNodes = nodesContainer.getDisconnectNodes();

            if (uncheckNodes.size() > 0) {
                probeNodes(uncheckNodes, canConnectNodes);
            }

            if (disconnectNodes.size() > 0) {
                probeNodes(disconnectNodes, canConnectNodes);
            }
        }
    }

    private void probeNodes(Map<String, Node> verifyNodes, Map<String, Node> canConnectNodes) {

        for (Map.Entry<String, Node> nodeEntry : verifyNodes.entrySet()) {
            Node node = nodeEntry.getValue();
            boolean needProbeNow = checkNeedProbeNow(node, verifyNodes);
            if (!needProbeNow) {
                continue;
            }
            int status = doProbe(node);

            if (status == PROBE_STATUS_IGNORE/* && !node.isSeedNode()*/) {
                continue;
            }

            verifyNodes.remove(node.getId());
            if (status == PROBE_STATUS_SUCCESS) {
                node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
                node.setStatus(NodeStatusEnum.CONNECTABLE);
                node.setFailCount(0);
                canConnectNodes.put(node.getId(), node);

                if (node.getLastProbeTime() == 0L) {
                    // 当lastProbeTime为0时，代表第一次探测且成功，只有在第一次探测成功时情况，才转发节点信息
                    doShare(node);
                }
            } else if (status == PROBE_STATUS_FAIL) {
                nodeManager.nodeConnectFail(node);
                nodeManager.getNodesContainer().getFailNodes().put(node.getId(), node);
            }
            node.setLastProbeTime(TimeService.currentTimeMillis());
        }
    }

    private boolean checkNeedProbeNow(Node node, Map<String, Node> verifyNodes) {
        // 探测间隔时间，根据失败的次数来决定，探测失败次数为failCount，探测间隔为probeInterval，定义分别如下：
        // failCount : 0-10 ，probeInterval = 60s
        // failCount : 11-20 ，probeInterval = 300s
        // failCount : 21-30 ，probeInterval = 600s
        // failCount : 31-50 ，probeInterval = 1800s
        // failCount : 51-100 ，probeInterval = 3600s
        // 当一个节点失败次数大于100时，将从节点列表中移除，除非再次收到该节点的分享，否则永远丢弃该节点

        long probeInterval;
        int failCount = node.getFailCount();

        if (failCount <= 10) {
            probeInterval = 60 * 1000L;
        } else if (failCount <= 20) {
            probeInterval = 300 * 1000L;
        } else if (failCount <= 30) {
            probeInterval = 600 * 1000L;
        } else if (failCount <= 50) {
            probeInterval = 1800 * 1000L;
        } else if (failCount <= 100) {
            probeInterval = 3600 * 1000L;
        } else {
            verifyNodes.remove(node.getId());
            return false;
        }

        return TimeService.currentTimeMillis() - node.getLastProbeTime() > probeInterval;
    }

    /*
     * 执行探测
     * @param int 探测结果 ： PROBE_STATUS_SUCCESS,成功  PROBE_STATUS_FAIL,失败  PROBE_STATUS_IGNORE,跳过（当断网时，也就是本地节点一个都没有连接时，不确定是对方连不上，还是本地没网，这时忽略）
     */
    private int doProbe(Node node) {

        if(node == null) {
            return PROBE_STATUS_FAIL;
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();

        node.setConnectedListener(() -> {
            node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
            node.getChannel().close();
        });

        node.setDisconnectListener(() -> {
            node.setChannel(null);

            if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTED) {
                future.complete(PROBE_STATUS_SUCCESS);
            } else if (nodeManager.getAvailableNodesCount() == 0) {
                future.complete(PROBE_STATUS_IGNORE);
            } else {
                future.complete(PROBE_STATUS_FAIL);
            }
        });

        boolean result = connectionManager.connection(node);
        if (!result) {
            return PROBE_STATUS_FAIL;
        }
        try {
            return future.get();
        } catch (Exception e) {
            Log.error(e);
            return PROBE_STATUS_IGNORE;
        }
    }

    private void doShare(Node node) {
        P2PNodeBody p2PNodeBody = new P2PNodeBody(node.getIp(), node.getPort());
        P2PNodeMessage message = new P2PNodeMessage(p2PNodeBody);
        broadcastHandler.broadcastToAllNode(message, null, true, 100);
    }
}
