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

package io.nuls.network.storage.service.impl;


import io.nuls.db.constant.DBConstant;
import io.nuls.db.service.DBService;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.network.model.Node;
import io.nuls.network.storage.constant.NetworkStorageConstant;
import io.nuls.network.storage.po.NetworkTransferTool;
import io.nuls.network.storage.po.NodePo;
import io.nuls.network.storage.service.NetworkStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.nuls.core.tools.str.StringUtils.bytes;

@Component
public class NetworkStorageServiceImpl implements NetworkStorageService, InitializingBean {

    @Autowired
    private DBService dbService;

    @Override
    public List<Node> getLocalNodeList() {
        List<NodePo> poList = getDbService().values(NetworkStorageConstant.DB_NAME_NETWORK_NODE, NodePo.class);
        if (poList == null) {
            return new ArrayList<>();
        }
        List<Node> nodeList = new ArrayList<>();
        for (NodePo po : poList) {
            nodeList.add(NetworkTransferTool.toNode(po));
        }
        return nodeList;
    }

    @Override
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

    @Override
    public void saveNode(Node node) {
        NodePo po = getDbService().getModel(NetworkStorageConstant.DB_NAME_NETWORK_NODE, bytes(node.getId()), NodePo.class);
        if (po != null) {
            NetworkTransferTool.toPojo(node, po);
        } else {
            po = NetworkTransferTool.toPojo(node);
        }
        getDbService().putModel(NetworkStorageConstant.DB_NAME_NETWORK_NODE, bytes(node.getId()), po);
    }

    @Override
    public void deleteNode(String nodeId) {
        getDbService().delete(NetworkStorageConstant.DB_NAME_NETWORK_NODE, bytes(nodeId));
    }

    @Override
    public void saveExternalIp(String ip) {
        getDbService().put(DBConstant.BASE_AREA_NAME, NetworkStorageConstant.DB_NAME_EXTERNAL_IP.getBytes(), ip.getBytes());
    }

    @Override
    public String getExternalIp() {
        byte[] bytes = getDbService().get(DBConstant.BASE_AREA_NAME, NetworkStorageConstant.DB_NAME_EXTERNAL_IP.getBytes());
        if (bytes != null) {
            return new String(bytes);
        }
        return null;
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
