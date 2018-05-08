package io.nuls.network.storage;


import io.nuls.db.model.ModelWrapper;
import io.nuls.db.service.DBService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class NetworkStorage {

    @Autowired
    private DBService dbService;

    public void init() {
        dbService.createArea(NetworkConstant.NODE_DB_AREA, NetworkConstant.NODE_DB_CACHE_SIZE);
    }

    public List<Node> getLocalNodeList(int size) {
        List<Node> nodeList = new ArrayList<>();
//        dbService.e
        return nodeList;
    }

    public List<Node> getLocalNodeList(int size, Set<String> ipSet) {
        List<Node> nodeList = new ArrayList<>();
//        dbService.e
        return nodeList;
    }

    public void saveNode(Node node) {

//        dbService.putModel(NetworkConstant.NODE_DB_AREA, node.getId(), node);
    }

    public void deleteNode(Node node) {

    }
}
