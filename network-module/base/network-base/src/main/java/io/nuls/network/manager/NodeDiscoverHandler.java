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

import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.message.GetVersionMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;

import java.util.Collection;

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

    private NodeManager2 nodesManager = NodeManager2.getInstance();

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
//        NodeMessageBody messageBody = new NodeMessageBody();
//        messageBody.setLength(size);
//        //将自己已经连接节点的ip地址发送给对方，避免对方节点重复给相同ip
//        List<String> ipList = new ArrayList<>();
//        for (Node node : nodesManager.getAvailableNodes()) {
//            ipList.add(node.getIp());
//        }
//        messageBody.setIpList(ipList);
//        GetNodesMessage message = new GetNodesMessage(messageBody);
//        List<Node> nodeList = new ArrayList<>(nodesManager.getAvailableNodes());
//        Collections.shuffle(nodeList);
//        for (int i = 0; i < nodeList.size(); i++) {
//            if (i == 2) {
//                break;
//            }
//            Node node = nodeList.get(i);
//            broadcastHandler.broadcastToNode(message, node, true);
//        }
    }

    /**
     * 每10秒询问一次当前连接的节点的最新高度和最新区块信息
     */

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        while (running) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Collection<Node> nodeList = nodesManager.getAvailableNodes();
            NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, networkParam.getPort(),
                    NulsContext.getInstance().getBestHeight(), NulsContext.getInstance().getBestBlock().getHeader().getHash());
            GetVersionMessage getVersionMessage = new GetVersionMessage(body);

            for (Node node : nodeList) {
                if (node.getType() == Node.OUT) {
                    broadcastHandler.broadcastToNode(getVersionMessage, node, true);
                }
            }
        }
    }
}
