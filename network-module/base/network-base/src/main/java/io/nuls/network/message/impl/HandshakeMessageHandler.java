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

import io.netty.channel.socket.SocketChannel;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.network.connection.netty.NioChannelMap;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.HandshakeMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;
import io.nuls.network.protocol.message.NodeMessageBody;
import io.nuls.network.protocol.message.NodesMessage;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.List;

public class HandshakeMessageHandler implements BaseNetworkMeesageHandler {

    private NodeManager nodeManager = NodeManager.getInstance();

    private NetworkParam networkParam = NetworkParam.getInstance();

    private static HandshakeMessageHandler instance = new HandshakeMessageHandler();

    private HandshakeMessageHandler() {

    }

    public static HandshakeMessageHandler getInstance() {
        return instance;
    }

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        HandshakeMessage handshakeMessage = (HandshakeMessage) message;
        SocketChannel socketChannel = NioChannelMap.get(node.getChannelId());
        NetworkMessageBody body = handshakeMessage.getMsgBody();

        boolean isServer = false;
        boolean isSuccess = false;

        if (body.getHandshakeType() == NetworkConstant.HANDSHAKE_SEVER_TYPE) {
            isSuccess = nodeManager.handshakeNode(NetworkConstant.NETWORK_NODE_OUT_GROUP, node, body);
        } else {
            isServer = true;
            isSuccess = nodeManager.handshakeNode(NetworkConstant.NETWORK_NODE_IN_GROUP, node, body);
        }

        if (!isSuccess) {
            if (socketChannel != null) {
                Log.debug("localInfo: " + socketChannel.localAddress().getHostString() + ":" + socketChannel.localAddress().getPort());
                Log.debug("handshake failed, close the connetion.");
                socketChannel.close();
                return null;
            }
        }
        //握手成功时，更新自己的外网ip地址
        node.setFailCount(0);
        node.setSeverPort(body.getSeverPort());
        node.setBestBlockHash(body.getBestBlockHash());
        node.setBestBlockHeight(body.getBestBlockHeight());
        if (node.getType() == Node.OUT) {
            nodeManager.saveNode(node);
        }
        if (nodeManager.isSeedNode(node.getIp())) {
            nodeManager.saveExternalIp(body.getNodeIp(), isServer);
        }

        if (!isServer) {
            body = new NetworkMessageBody(NetworkConstant.HANDSHAKE_CLIENT_TYPE, networkParam.getPort(),
                    NulsContext.getInstance().getBestHeight(), NulsContext.getInstance().getBestBlock().getHeader().getHash(),
                    socketChannel.remoteAddress().getHostString());
            return new NetworkEventResult(true, new HandshakeMessage(body));
        } else {
            //如果是服务器方，在握手成功后，返回当前自己已经连接的所有节点信息
            List<Node> nodeList = nodeManager.getCanConnectNodes();
            NodeMessageBody messageBody = new NodeMessageBody(nodeList);
            NodesMessage nodesMessage = new NodesMessage(messageBody);
            return new NetworkEventResult(true, nodesMessage);
        }
    }
}
