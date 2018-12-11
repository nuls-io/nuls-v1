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

package io.nuls.network.storage.service;

import com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter;
import io.nuls.network.model.Node;
import io.nuls.network.storage.po.NodeContainerPo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 提供节点信息的本地存储服务
 * 这里应该只存储主动连接的节点
 */
public interface NetworkStorageService {

    Node getNode(String nodeId);

    List<Node> getAllNodes();

    List<Node> getNodeList(int size, Set<String> ipSet);

    void saveNode(Node node);

    void deleteNode(String nodeId);

    void saveExternalIp(String ip);

    String getExternalIp();

    void saveNodes(Map<String, Node> disConnectNodes, Map<String, Node> canConnectNodes, Map<String, Node> failNodes, Map<String, Node> uncheckNodes, Map<String, Node> connectedNodes);

    NodeContainerPo loadNodeContainer();
}
