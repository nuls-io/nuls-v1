/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.network.netty.container;

import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.NodeGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupContainer {
    private Map<String, NodeGroup> nodeGroups = new ConcurrentHashMap<>();

    public GroupContainer() {
        nodeGroups.put(NetworkConstant.NETWORK_NODE_IN_GROUP, new NodeGroup(NetworkConstant.NETWORK_NODE_IN_GROUP));
        nodeGroups.put(NetworkConstant.NETWORK_NODE_OUT_GROUP, new NodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP));
    }

    public int getNodesCount() {
        int total = 0;

        NodeGroup inGroup = nodeGroups.get(NetworkConstant.NETWORK_NODE_IN_GROUP);
        if (inGroup != null) {
            total += inGroup.size();
        }
        NodeGroup outGroup = nodeGroups.get(NetworkConstant.NETWORK_NODE_OUT_GROUP);
        if (outGroup != null) {
            total += outGroup.size();
        }

        return total;
    }

    public NodeGroup getNodeGroup(String groupName) {
        NodeGroup group = nodeGroups.get(groupName);
        return group;
    }

    public int getNodesCount(String groupName) {
        NodeGroup group = nodeGroups.get(groupName);
        if (group != null) {
            return group.size();
        }
        return 0;
    }

    public Map<String, NodeGroup> getNodeGroups() {
        return nodeGroups;
    }

    public void setNodeGroups(Map<String, NodeGroup> nodeGroups) {
        this.nodeGroups = nodeGroups;
    }
}
