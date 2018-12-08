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

package io.nuls.network.netty.manager;

import io.nuls.core.tools.log.Log;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.netty.container.GroupContainer;
import io.nuls.network.netty.container.NodesContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NodeManager {

    private static NodeManager instance = new NodeManager();

    private NetworkParam networkParam;

    private NodesContainer nodesContainer;
    private GroupContainer groupContainer;

    public static NodeManager getInstance() {
        return instance;
    }

    private NodeManager() {
        nodesContainer = new NodesContainer();
        groupContainer = new GroupContainer();
    }


    public void loadDatas() {

        Map<String, Node> allNodes = nodesContainer.getAllNodes();

        //加载数据库的节点信心 TODO


        // 合并种子节点
        for(Map.Entry<String, Node> nodeEntry : allNodes.entrySet()) {
            Node node = nodeEntry.getValue();
            if(node.isSeedNode()) {
                node.setSeedNode(false);
            }
        }
        for(String seedId : networkParam.getSeedIpList()) {

            if(allNodes.containsKey(seedId)) {
                allNodes.get(seedId).setSeedNode(true);
                continue;
            }
            try {
                String[] ipPort = seedId.split(":");
                String ip = ipPort[0];
                int port = Integer.parseInt(ipPort[1]);

                Node node = new Node(ip, port, Node.OUT);
                allNodes.put(seedId, node);
            } catch (Exception e) {
                Log.warn("the seed config is warn of {}", seedId);
            }
        }

    }

    public Collection<Node> getAvailableNodes() {
        return nodesContainer.getConnectedNodes().values();
    }

    public int getAvailableNodesCount() {
        return nodesContainer.getConnectedNodes().size();
    }

    public Collection<Node> getCanConnectNodes() {
        return nodesContainer.getAllNodes().values();
    }

    public NodeGroup getNodeGroup(String groupName) {
        return null;
    }

    public Map<String, Node> getNodes() {
        return null;
    }

    public Node getNode(String nodeId) {
        return null;
    }

    public void removeNode(String nodeId) {
        return;
    }

    public void reset() {
    }

    public void initNetworkParam(NetworkParam networkParam) {
        this.networkParam = networkParam;
    }

    public void nodeConnectSuccess(Node node) {
        nodesContainer.getConnectedNodes().put(node.getId(), node);
        nodesContainer.getAllNodes().remove(node.getId());
    }


    public void nodeConnectFail(Node node) {
        nodesContainer.getAllNodes().remove(node.getId());
    }

    public void nodeConnectDisconnect(Node node) {
        nodesContainer.getConnectedNodes().remove(node.getId());
        nodesContainer.getDisconnectNodes().put(node.getId(), node);
    }
}
