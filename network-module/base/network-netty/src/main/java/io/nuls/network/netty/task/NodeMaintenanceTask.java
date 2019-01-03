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
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeConnectStatusEnum;
import io.nuls.network.netty.manager.ConnectionManager;
import io.nuls.network.netty.manager.NodeManager;

import java.util.*;

/**
 * 节点维护任务
 *
 * @author: ln
 * @date: 2018/12/8
 */
public class NodeMaintenanceTask implements Runnable {

    private final NetworkParam networkParam = NetworkParam.getInstance();
    private final NodeManager nodeManager = NodeManager.getInstance();
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void process() {
        List<Node> needConnectNodes = getNeedConnectNodes();
        if (needConnectNodes == null || needConnectNodes.size() == 0) {
            Log.info("-------needConnectNodes is null");
            return;
        }

        for (Node node : needConnectNodes) {
            node.setType(Node.OUT);
            connectionNode(node);
        }
    }

    private boolean connectionNode(Node node) {
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTING);

        node.setRegisterListener(() -> Log.debug("new node {} try connecting!", node.getId()));

        node.setConnectedListener(() -> nodeManager.nodeConnectSuccess(node));

        node.setDisconnectListener(() -> {
            Log.info("-----------out node disconnect:" + node.getId());
            nodeManager.nodeConnectDisconnect(node);
        });
        return connectionManager.connection(node);
    }

    private List<Node> getNeedConnectNodes() {

        Collection<Node> avaliableNodes = nodeManager.getAvailableNodes();
        Log.info("---------avaliableNodes.size:" + avaliableNodes.size());
        if (avaliableNodes.size() >= networkParam.getMaxOutCount()) {
            return null;
        }

        Collection<Node> canConnectNodes = nodeManager.getCanConnectNodes();
        Log.info("---------canConnectNodes.size:" + canConnectNodes.size());
        if (canConnectNodes.size() == 0) {
            return null;
        }

        List<Node> nodeList = new ArrayList<>(canConnectNodes);

        nodeList.removeAll(avaliableNodes);

        int maxCount = networkParam.getMaxOutCount() - avaliableNodes.size();
        if (nodeList.size() < maxCount) {
            return nodeList;
        }

        Collections.shuffle(nodeList);

        return nodeList.subList(0, maxCount);
    }
}
