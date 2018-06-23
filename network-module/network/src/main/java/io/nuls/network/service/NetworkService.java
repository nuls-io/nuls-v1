/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.network.service;

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.BroadcastResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ln on 2018/5/5.
 */
public interface NetworkService {

    /**
     * 断开一个已连接的节点
     * Disconnect the connection with the node
     *
     * @param nodeId the id of node
     */
    void removeNode(String nodeId);

    /**
     * 获取一个节点
     * get node by id
     *
     * @param nodeId the id of node
     * @return
     */
    Node getNode(String nodeId);

    /**
     * 获取所有节点
     * get all nodes
     *
     * @return
     */
    Map<String, Node> getNodes();

    /**
     * 获取已连接的节点
     * get connected nodes
     *
     * @return
     */
    Collection<Node> getAvailableNodes();

    /**
     * 获取可连接的节点
     * get connectable nodes
     *
     * @return
     */
    List<Node> getCanConnectNodes();

    /**
     * 根据名字获取节点组
     * get NodeGroup by name
     *
     * @param groupName
     * @return
     */
    NodeGroup getNodeGroup(String groupName);

    /**
     * 发送消息
     * Send message to all connected nodes
     *
     * @param nulsData message
     * @param asyn     Whether or not asynchronous
     * @return
     */
    BroadcastResult sendToAllNode(BaseNulsData nulsData, boolean asyn);

    /**
     * 发送消息
     * Send message to all connected nodes
     *
     * @param event
     * @param excludeNode node that does not need to be send
     * @param asyn        Whether or not asynchronous
     * @return
     */
    BroadcastResult sendToAllNode(BaseNulsData event, Node excludeNode, boolean asyn);

    /**
     * send message to node
     *
     * @param event
     * @param node
     * @param asyn  Whether or not asynchronous
     * @return
     */
    BroadcastResult sendToNode(BaseNulsData event, Node node, boolean asyn);

    /**
     * 发送消息给节点组
     * send message to nodeGroup
     *
     * @param event
     * @param groupName
     * @param asyn
     * @return
     */
    BroadcastResult sendToGroup(BaseNulsData event, String groupName, boolean asyn);

    /**
     * 发送消息给节点组
     * send message to nodeGroup
     *
     * @param event
     * @param groupName
     * @param excludeNode node that does not need to be send
     * @param asyn
     * @return
     */
    BroadcastResult sendToGroup(BaseNulsData event, String groupName, Node excludeNode, boolean asyn);

    /**
     * 重置网络
     * reset network module
     */
    void reset();

    /**
     * 获取网络配置信息
     * Get network configuration information
     *
     * @return
     */
    NetworkParam getNetworkParam();
}
