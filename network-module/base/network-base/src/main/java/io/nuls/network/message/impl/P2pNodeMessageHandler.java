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

package io.nuls.network.message.impl;

import io.nuls.network.cache.NodeCacheManager;
import io.nuls.network.manager.BroadcastHandler;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.P2PNodeBody;
import io.nuls.network.protocol.message.P2PNodeMessage;
import io.nuls.protocol.message.base.BaseMessage;

public class P2pNodeMessageHandler implements BaseNetworkMeesageHandler {

    private NodeManager nodeManager = NodeManager.getInstance();

    private static P2pNodeMessageHandler instance = new P2pNodeMessageHandler();

    private NodeCacheManager nodeCacheManager = NodeCacheManager.getInstance();

    private BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private P2pNodeMessageHandler() {

    }

    public static P2pNodeMessageHandler getInstance() {
        return instance;
    }

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        P2PNodeMessage nodeMessage = (P2PNodeMessage) message;
        P2PNodeBody nodeBody = nodeMessage.getMsgBody();
        P2PNodeBody cacheBody = nodeCacheManager.getNode(nodeBody.getId());
        //已经缓存则什么都不处理
        if (cacheBody != null) {
            System.out.println("---------------------cacheBody is not null------------------------------");
            return null;
        }
        //尝试建立连接
        Node newNode = new Node(nodeBody.getNodeIp(), nodeBody.getSeverPort(), nodeBody.getSeverPort(), Node.OUT);
        nodeManager.addNode(newNode);
        nodeCacheManager.cacheNode(nodeBody);
        //广播交易
        broadcastHandler.broadcastToAllNode(message, node, true);
        return null;
    }
}
