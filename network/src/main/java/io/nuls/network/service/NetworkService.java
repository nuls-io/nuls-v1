/**
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
 */
package io.nuls.network.service;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.NodeGroup;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface NetworkService {

    void init();

    void start();

    void shutdown();

    void addNode(Node node);

    void removeNode(String nodeId);

    void blackNode(String nodeId, int status);

    void addNodeToGroup(String groupName, Node node);

    void addNodeToGroup(String area,String groupName,Node node);

    void removeNodeFromGroup(String groupName, String nodeId);

    void removeNodeFromGroup(String area,String groupName,Node node);

    void addNodeGroup(NodeGroup nodeGroup);

    void addNodeGroup(String area,NodeGroup nodeGroup);

    BroadcastResult sendToAllNode(BaseEvent event);

    BroadcastResult sendToAllNode(String area, BaseEvent event);

    BroadcastResult sendToAllNode(BaseEvent event, String excludeNodeId);

    BroadcastResult sendToAllNode(String area, BaseEvent event, String excludeNodeId);

    BroadcastResult sendToAllNode(byte[] data);

    BroadcastResult sendToAllNode(String area, byte[] data);

    BroadcastResult sendToAllNode(byte[] data, String excludeNodeId);

    BroadcastResult sendToAllNode(String area, byte[] data, String excludeNodeId);

    BroadcastResult sendToNode(BaseEvent event, String nodeId);

    BroadcastResult sendToNode(String area, BaseEvent event, String nodeId);

    BroadcastResult sendToNode(byte[] data, String nodeId);

    BroadcastResult sendToNode(String area, byte[] data, String nodeId);

    BroadcastResult sendToGroup(BaseEvent event, String groupName);

    BroadcastResult sendToGroup(String area,BaseEvent event,String groupName);

    BroadcastResult sendToGroup(BaseEvent event, String groupName, String excludeNodeId);

    BroadcastResult sendToGroup(String area, BaseEvent event, String groupName, String excludeNodeId);

    BroadcastResult sendToGroup(byte[] data, String groupName);

    BroadcastResult sendToGroup(String area, byte[] data, String groupName);

    BroadcastResult sendToGroup(byte[] data, String groupName, String excludeNodeId);

    BroadcastResult sendToGroup(String area, byte[] data, String groupName, String excludeNodeId);

}
