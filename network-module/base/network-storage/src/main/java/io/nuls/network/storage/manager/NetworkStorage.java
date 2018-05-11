package io.nuls.network.storage.manager;


import io.nuls.db.service.DBService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.storage.po.NetworkTransferTool;
import io.nuls.network.storage.po.NodePo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.nuls.core.tools.str.StringUtils.bytes;

@Component
public class NetworkStorage {

    @Autowired
    private DBService dbService;

    public void init() {
        dbService.createArea(NetworkConstant.NODE_DB_AREA);
    }

    public List<Node> getLocalNodeList(int size) {
        List<NodePo> poList = dbService.values(NetworkConstant.NODE_DB_AREA, NodePo.class);
        if (poList == null) {
            return new ArrayList<>();
        }
        if (size > poList.size()) {
            size = poList.size();
        }

        poList = poList.subList(0, size);
        List<Node> nodeList = new ArrayList<>();
        for (NodePo po : poList) {
            nodeList.add(NetworkTransferTool.toNode(po));
        }
        return nodeList;
    }

    public List<Node> getLocalNodeList(int size, Set<String> ipSet) {
        List<Node> nodeList = new ArrayList<>();
        List<NodePo> poList = dbService.values(NetworkConstant.NODE_DB_AREA, NodePo.class);
        if (poList == null) {
            return nodeList;
        }
        int count = 0;
        for (int i = poList.size() - 1; i >= 0; i--) {
            NodePo po = poList.get(i);
            if (ipSet.contains(po.getIp())) {
                continue;
            }
            nodeList.add(NetworkTransferTool.toNode(po));
            count++;
            if (count >= size) {
                break;
            }
        }
        return nodeList;
    }

    public void saveNode(Node node) {
        NodePo po = dbService.getModel(NetworkConstant.NODE_DB_AREA, bytes(node.getId()), NodePo.class);
        if (po != null) {
            NetworkTransferTool.toPojo(node, po);
        } else {
            po = NetworkTransferTool.toPojo(node);
        }
        dbService.putModel(NetworkConstant.NODE_DB_AREA, bytes(node.getId()), po);
    }

    public void deleteNode(Node node) {
        dbService.delete(NetworkConstant.NODE_DB_AREA, bytes(node.getId()));
    }
}
