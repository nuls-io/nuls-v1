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

import io.nuls.kernel.context.NulsContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;
import io.nuls.network.netty.broadcast.BroadcastHandler;
import io.nuls.network.netty.manager.NodeManager;
import io.nuls.network.protocol.message.GetVersionMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;

import java.util.Collection;

/**
 * 维护节点高度的定时任务
 * @author: ln
 * @date: 2018/12/8
 */
public class GetNodeVersionTask implements Runnable {

    private final NodeManager nodeManager = NodeManager.getInstance();
    private final BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private NetworkParam networkParam = NetworkParam.getInstance();

    public GetNodeVersionTask() {
    }

    @Override
    public void run() {

        Collection<Node> connectedNodes = nodeManager.getAvailableNodes();

        if(connectedNodes == null) {
            return;
        }

        NetworkMessageBody body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, networkParam.getPort(),
                NulsContext.getInstance().getBestHeight(), NulsContext.getInstance().getBestBlock().getHeader().getHash());
        GetVersionMessage getVersionMessage = new GetVersionMessage(body);

        for(Node node : connectedNodes) {
            if (node.getType() == Node.OUT) {
                broadcastHandler.broadcastToNode(getVersionMessage, node, true);
            }
        }
    }

}
