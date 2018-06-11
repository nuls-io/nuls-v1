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

package io.nuls.network.storage.po;

import io.nuls.network.model.Node;

public class NetworkTransferTool {

    public static NodePo toPojo(Node node) {
        NodePo po = new NodePo();
        po.setId(node.getId());
        po.setIp(node.getIp());
        po.setPort(node.getSeverPort());
        po.setLastTime(node.getLastTime());
        po.setLastFailTime(node.getLastFailTime());
        po.setFailCount(node.getFailCount());
        return po;
    }

    public static void toPojo(Node node, NodePo po) {
        po.setId(node.getId());
        po.setIp(node.getIp());
        po.setPort(node.getSeverPort());
        po.setLastTime(node.getLastTime());
        po.setLastFailTime(node.getLastFailTime());
        po.setFailCount(node.getFailCount());
    }

    public static Node toNode(NodePo po) {
        Node node = new Node();
        node.setId(po.getId());
        node.setIp(po.getIp());
        node.setPort(po.getPort());
        node.setSeverPort(po.getPort());
        node.setFailCount(0);
        node.setLastTime(po.getLastTime());
        node.setLastFailTime(po.getLastFailTime());
        return node;
    }
}
