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

package io.nuls.network.netty.service.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.BroadcastResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.netty.broadcast.BroadcastHandler;
import io.nuls.network.netty.manager.NodeManager;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class NetworkServiceImpl implements NetworkService {

    private NodeManager nodeManager = NodeManager.getInstance();

    private BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    @Override
    public void removeNode(String nodeId) {
        nodeManager.removeNode(nodeId);
    }

    @Override
    public Node getNode(String nodeId) {
        return nodeManager.getNode(nodeId);
    }

    @Override
    public Map<String, Node> getNodes() {
        return nodeManager.getNodes();
    }

    @Override
    public Collection<Node> getAvailableNodes() {
        return nodeManager.getAvailableNodes();
    }

    @Override
    public List<Node> getCanConnectNodes() {
        return new ArrayList<>(nodeManager.getCanConnectNodes());
    }

    @Override
    public NodeGroup getNodeGroup(String groupName) {
        return nodeManager.getNodeGroup(groupName);
    }

    @Override
    public BroadcastResult sendToAllNode(BaseNulsData nulsData, boolean asyn, int percent) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcastToAllNode(baseMessage, null, asyn, percent);
    }

    @Override
    public BroadcastResult sendToAllNode(BaseNulsData nulsData, Node excludeNode, boolean asyn, int percent) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcastToAllNode(baseMessage, excludeNode, asyn,percent);
    }

    @Override
    public BroadcastResult sendToNode(BaseNulsData nulsData, Node node, boolean asyn) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcastToNode(baseMessage, node, asyn);
    }

    @Override
    public BroadcastResult sendToGroup(BaseNulsData nulsData, String groupName, boolean asyn) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcastToNodeGroup(baseMessage, groupName, asyn);
    }

    @Override
    public BroadcastResult sendToGroup(BaseNulsData nulsData, String groupName, Node excludeNode, boolean asyn) {
        BaseMessage baseMessage = (BaseMessage) nulsData;
        return broadcastHandler.broadcastToNodeGroup(baseMessage, groupName, excludeNode, asyn);
    }

    @Override
    public void reset() {
        Log.warn("------network reset");
        nodeManager.reset();
    }

    @Override
    public NetworkParam getNetworkParam() {
        return NetworkParam.getInstance();
    }
}
