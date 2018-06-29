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
package io.nuls.network.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vivi
 */
public class NodeGroup {
    private String groupName;
    private Set<NodeArea> areaSet;
    private Map<String, Node> nodeMap;

    public NodeGroup(String groupName) {
        this.groupName = groupName;
        nodeMap = new ConcurrentHashMap<>();
        areaSet = ConcurrentHashMap.newKeySet();
    }

    public Map<String, Node> getNodes() {
        return nodeMap;
    }

    public void addNode(Node p) {
        if (nodeMap.containsKey(p.getId())) {
            return;
        }
        this.nodeMap.put(p.getId(), p);
        p.addToGroup(this);
    }

    public void removeNode(Node node) {
        this.nodeMap.remove(node.getId());
    }

    public void removeNode(String nodeId) {
        this.nodeMap.remove(nodeId);
    }

    public int size() {
        return nodeMap.size();
    }

    public void removeAll() {
        nodeMap = new ConcurrentHashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{NodeGroup:{groupName:'").append(this.getName()).append("',");
        sb.append("nodeMap:[");
        for (Node n : nodeMap.values()) {
            sb.append(n.toString()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        sb.append("],areaSet:[");
        for (NodeArea na : areaSet) {
            sb.append(na.toString()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]}");

        return sb.toString();
    }


    public void setName(String name) {
        this.groupName = name;
    }

    public String getName() {
        return groupName;
    }

    public Set<NodeArea> getAreaSet() {
        return this.areaSet;
    }

    public int getAreaCount() {
        return this.areaSet.size();
    }

    public void addtoArea(NodeArea nodeArea) {
        if (nodeArea != null) {
            this.areaSet.add(nodeArea);
        }
    }

    public void removeFromArea(NodeArea nodeArea) {
        if (nodeArea != null) {
            this.areaSet.remove(nodeArea);
        }
    }
}
