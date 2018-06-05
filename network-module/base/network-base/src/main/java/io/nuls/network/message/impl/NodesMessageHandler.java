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

import io.nuls.core.tools.network.IpUtil;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.NodeMessageBody;
import io.nuls.network.protocol.message.NodesMessage;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.Map;
import java.util.Set;

public class NodesMessageHandler implements BaseNetworkMeesageHandler {

    private static NodesMessageHandler instance = new NodesMessageHandler();

    private NodesMessageHandler() {

    }

    public static NodesMessageHandler getInstance() {
        return instance;
    }

    private NodeManager nodeManager = NodeManager.getInstance();

    private Set<String> localIps = IpUtil.getIps();

    @Override
    public NetworkEventResult process(BaseMessage message, Node node) {
        NodesMessage nodesMessage = (NodesMessage) message;
        NodeMessageBody body = nodesMessage.getMsgBody();

        boolean exist = false;
        Map<String, Node> outNodes = nodeManager.getNodes();
        for (Node newNode : body.getNodeList()) {
            if (localIps.contains(newNode.getIp())) {
                continue;
            }
            exist = false;
            for (Node outNode : outNodes.values()) {
                if (outNode.getIp().equals(newNode.getIp())) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                newNode.setType(Node.OUT);
                newNode.setStatus(Node.CLOSE);
                newNode.setId(null);
                nodeManager.addNode(newNode);
            }
        }
        return null;
    }
}
