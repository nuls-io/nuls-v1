package io.nuls.network.entity;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class NodeGroup {
    private String groupName;
    private CopyOnWriteArrayList<Node> nodes;

    public NodeGroup(String groupName) {
        this.groupName = groupName;
        nodes = new CopyOnWriteArrayList<>();
    }

    public CopyOnWriteArrayList<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node p) {
        for (Node node : nodes) {
            if (node.getHash().toString().equals(p.getHash().toString())) {
                return;
            }
        }
        this.nodes.add(p);
    }

    public void removeNode(Node p) {
        this.nodes.remove(p);
    }

    public int size() {
        return nodes.size();
    }

    public void removeAll() {
        nodes.clear();
    }

    @Override
    public String toString() {
        return "";
    }


    public void setName(String name) {
        this.groupName = name;
    }

    public String getName() {
        return groupName;
    }
}
