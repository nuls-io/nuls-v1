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
import io.nuls.network.netty.manager.ConnectionManager;
import io.nuls.network.netty.manager.NodeManager;

import java.util.*;

/**
 * 节点维护任务
 * @author: ln
 * @date: 2018/12/8
 */
public class NodeMaintenanceTask implements Runnable {

    private final NetworkParam networkParam = NetworkParam.getInstance();
    private final NodeManager nodeManager = NodeManager.getInstance();
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();

    @Override
    public void run() {

        List<Node> needConnectNodes = getNeedConnectNodes();

        if(needConnectNodes == null) {
            return;
        }

        for(Node node : needConnectNodes) {
            boolean success = connectionNode(node);
            if(success) {
                node.setType(Node.OUT);
            } else {
                //fail
                nodeManager.nodeConnectFail(node);
            }
        }
    }

    private boolean connectionNode(Node node) {

        node.setRegisterListener(new EventListener() {
            @Override
            public void action() {
                Log.debug("new node {} try connecting!", node.getId());
            }
        });

        node.setConnectedListener(new EventListener() {
            @Override
            public void action() {
                Log.info("node {} connect success !", node.getId());

                nodeManager.nodeConnectSuccess(node);
            }
        });

        node.setDisconnectListener(new EventListener() {
            @Override
            public void action() {
                Log.info("node {} disconnect !", node.getId());
                nodeManager.nodeConnectDisconnect(node);
            }
        });

        boolean success = connectionManager.connection(node);
        return success;
    }

    private List<Node> getNeedConnectNodes() {

        Collection<Node> avaliableNodes = nodeManager.getAvailableNodes();

        if(avaliableNodes.size() >= networkParam.getMaxOutCount()) {
            return null;
        }

        Collection<Node> allNodes = nodeManager.getCanConnectNodes();
        if(allNodes.size() <= avaliableNodes.size()) {
            return null;
        }

        List<Node> nodeList = new ArrayList<>(allNodes);

        nodeList.removeAll(avaliableNodes);

        int maxCount = networkParam.getMaxOutCount() - avaliableNodes.size();
        if(nodeList.size() < maxCount) {
            return nodeList;
        }

        Collections.shuffle(nodeList);

        return nodeList.subList(0, maxCount);
    }
}
