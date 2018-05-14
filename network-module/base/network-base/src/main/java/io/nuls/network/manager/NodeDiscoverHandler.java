/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.manager;

import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.protocol.message.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NodeDiscoverHandler implements Runnable {

    private static NodeDiscoverHandler instance = new NodeDiscoverHandler();

    private NodeDiscoverHandler() {
    }

    public static NodeDiscoverHandler getInstance() {
        return instance;
    }

    private NetworkParam networkParam = NetworkParam.getInstance();

    private NodeManager nodesManager = NodeManager.getInstance();

    private BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private boolean running = false;

    public void start() {
        running = true;
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "NetworkNodeDiscover", this);
    }

    /**
     * Inquire more of the other nodes to the connected nodes
     *
     * @param size
     */
    public void findOtherNode(int size) {
        NodeMessageBody messageBody = new NodeMessageBody();
        messageBody.setLength(size);
        GetNodesMessage message = new GetNodesMessage(messageBody);
        List<Node> nodeList = new ArrayList<>(nodesManager.getAvailableNodes());
        Collections.shuffle(nodeList);
        for (int i = 0; i < nodeList.size(); i++) {
            if (i == 2) {
                break;
            }
            Node node = nodeList.get(i);
            broadcastHandler.broadcastToNode(message, node, true);
        }
    }

    /**
     * 每10秒询问一次当前连接的节点的最新高度和最新区块信息
     * 每30秒询问一次已连接的节点，向他们询问其他更多可连接的节点的IP地址
     */
    private int count = 2;

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        while (running) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;

            Collection<Node> nodeList = nodesManager.getAvailableNodes();
            NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, networkParam.getPort(),
                    10001, NulsDigestData.calcDigestData("a1b2c3d4e5gf6g7h8i9j10".getBytes()));
            GetVersionMessage getVersionMessage = new GetVersionMessage(body);
            GetNodesIpMessage getNodesIpMessage = new GetNodesIpMessage();

            for (Node node : nodeList) {
                if (node.getType() == Node.OUT) {
                    broadcastHandler.broadcastToNode(getVersionMessage, node, true);
                }
                if (count == 3) {
                    broadcastHandler.broadcastToNode(getNodesIpMessage, node, true);
                }
            }
            if (count == 3) {
                count = 0;
            }
        }
    }
}
