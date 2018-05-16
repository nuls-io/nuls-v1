package io.nuls.network.storage.service.impl;


import io.nuls.db.service.DBService;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.network.entity.Node;
import io.nuls.network.storage.constant.NetworkStorageConstant;
import io.nuls.network.storage.po.NetworkTransferTool;
import io.nuls.network.storage.po.NodePo;
import io.nuls.network.storage.service.NetworkStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.nuls.core.tools.str.StringUtils.bytes;

@Component
public class NetworkStorageServiceImpl implements NetworkStorageService , InitializingBean {

    @Autowired
    private DBService dbService;

    public List<Node> getLocalNodeList(int size) {
        List<NodePo> poList = getDbService().values(NetworkStorageConstant.DB_NAME_NETWORK_NODE, NodePo.class);
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
        List<NodePo> poList = getDbService().values(NetworkStorageConstant.DB_NAME_NETWORK_NODE, NodePo.class);
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
        NodePo po = getDbService().getModel(NetworkStorageConstant.DB_NAME_NETWORK_NODE, bytes(node.getId()), NodePo.class);
        if (po != null) {
            NetworkTransferTool.toPojo(node, po);
        } else {
            po = NetworkTransferTool.toPojo(node);
        }
        getDbService().putModel(NetworkStorageConstant.DB_NAME_NETWORK_NODE, bytes(node.getId()), po);
    }

    public void deleteNode(String nodeId) {
        getDbService().delete(NetworkStorageConstant.DB_NAME_NETWORK_NODE, bytes(nodeId));
    }


    private DBService getDbService() {
        if (dbService == null) {
            dbService = NulsContext.getServiceBean(DBService.class);
        }
        return dbService;
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        getDbService().createArea(NetworkStorageConstant.DB_NAME_NETWORK_NODE);
    }
}
