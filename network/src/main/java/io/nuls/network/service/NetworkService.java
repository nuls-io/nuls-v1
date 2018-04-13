/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.service;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;
import io.nuls.network.entity.param.AbstractNetworkParam;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface NetworkService {

    void init();

    void start();

    void shutdown();

    void removeNode(String nodeId);

    void removeNode(String nodeId, int type);

    void deleteNode(String nodeId);

    Map<String, Node> getNodes();

    Node getNode(String nodeId);

    List<Node> getAvailableNodes();

    Set<String> getNodesIp();

    boolean addNode(Node node);

    boolean addConnNode(Node node);

    boolean isSeedNode(String ip);

    boolean isSeed();

    boolean handshakeNode(String groupName, Node node);

    void blackNode(String nodeId, int status);

    boolean addNodeToGroup(String groupName, Node node);

    void removeNodeFromGroup(String groupName, String nodeId);

    void addNodeGroup(NodeGroup nodeGroup);

    void removeNodeGroup(String groupName);

    NodeGroup getNodeGroup(String groupName);

    AbstractNetworkParam getNetworkParam();

    BroadcastResult sendToAllNode(BaseEvent event, boolean asyn);

    BroadcastResult sendToAllNode(BaseEvent event, String excludeNodeId, boolean asyn);

    BroadcastResult sendToNode(BaseEvent event, String nodeId, boolean asyn);

    BroadcastResult sendToGroup(BaseEvent event, String groupName, boolean asyn);

    BroadcastResult sendToGroup(BaseEvent event, String groupName, String excludeNodeId, boolean asyn);

    void receiveMessage(ByteBuffer buffer, Node node);

    void reset();

}
