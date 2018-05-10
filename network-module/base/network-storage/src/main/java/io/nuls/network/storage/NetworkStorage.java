package io.nuls.network.storage;


import io.nuls.db.service.DBService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.nuls.core.tools.str.StringUtils.bytes;

@Component
public class NetworkStorage {

    @Autowired
    private DBService dbService;

    public void init() {
        dbService.createArea(NetworkConstant.NODE_DB_AREA, NetworkConstant.NODE_DB_CACHE_SIZE);
    }

    public List<Node> getLocalNodeList(int size) {
        List<Node> nodeList = dbService.values(NetworkConstant.NODE_DB_AREA, Node.class);
        if(nodeList == null) {
            nodeList = new ArrayList<>();
        }
        if (size > nodeList.size()) {
            size = nodeList.size();
        }
        return nodeList.subList(0, size);
    }

    public List<Node> getLocalNodeList(int size, Set<String> ipSet) {
        List<Node> nodeList = new ArrayList<>();
        List<Node> localList = dbService.values(NetworkConstant.NODE_DB_AREA, Node.class);
        if(localList == null) {
            localList = new ArrayList<>();
        }
        int count = 0;
        for (int i = localList.size() - 1; i >= 0; i--) {
            Node node = localList.get(i);
            if (ipSet.contains(node.getIp())) {
                continue;
            }
            nodeList.add(node);
            count++;
            if (count >= size) {
                break;
            }
        }
        return nodeList;
    }

    public void saveNode(Node node) {
        dbService.putModel(NetworkConstant.NODE_DB_AREA, bytes(node.getId()), node);
    }

    public void deleteNode(Node node) {

    }
}
