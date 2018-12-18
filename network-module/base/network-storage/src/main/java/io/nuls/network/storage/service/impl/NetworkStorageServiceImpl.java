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


import io.nuls.core.tools.cfg.ConfigLoader;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.db.constant.DBConstant;
import io.nuls.db.service.DBService;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.lite.core.bean.InitializingBean;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.Node;
import io.nuls.network.storage.constant.NetworkStorageConstant;
import io.nuls.network.storage.po.NetworkTransferTool;
import io.nuls.network.storage.po.NodeContainerPo;
import io.nuls.network.storage.po.NodePo;
import io.nuls.network.storage.service.NetworkStorageService;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

import static io.nuls.core.tools.str.StringUtils.bytes;

@Component
public class NetworkStorageServiceImpl implements NetworkStorageService, InitializingBean {


    @Autowired
    private DBService dbService;

    @Override
    public Node getNode(String nodeId) {
        NodePo nodePo = getDbService().getModel(NetworkStorageConstant.DB_NAME_NETWORK_NODE, bytes(nodeId), NodePo.class);
        if (nodePo == null) {
            return null;
        }
        return NetworkTransferTool.toNode(nodePo);
    }

    @Override
    public List<Node> getAllNodes() {
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
    public List<Node> getNodeList(int size, Set<String> ipSet) {
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

    @Override
    public void saveNodes(Map<String, Node> disConnectNodes, Map<String, Node> canConnectNodes, Map<String, Node> failNodes, Map<String, Node> uncheckNodes, Map<String, Node> connectedNodes) {
        NodeContainerPo containerPo = createNodeContainerPo(disConnectNodes, canConnectNodes, failNodes, uncheckNodes, connectedNodes);
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(getStoreageFile());
            oos = new ObjectOutputStream(fos);
            oos.writeObject(containerPo);
        } catch (FileNotFoundException e) {
            Log.error(e);
        } catch (IOException e) {
            Log.error(e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public NodeContainerPo loadNodeContainer() {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(getStoreageFile());
            ois = new ObjectInputStream(fis);
            NodeContainerPo containerPo = (NodeContainerPo) ois.readObject();
            return containerPo;
        } catch (FileNotFoundException e) {
//            Log.error(e);
        } catch (IOException e) {
            Log.error(e);
        } catch (ClassNotFoundException e) {
            Log.error(e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private NodeContainerPo createNodeContainerPo(Map<String, Node> disConnectNodes,
                                                  Map<String, Node> canConnectNodes,
                                                  Map<String, Node> failNodes,
                                                  Map<String, Node> uncheckNodes,
                                                  Map<String, Node> connectedNodes) {
        NodeContainerPo containerPo = new NodeContainerPo();
        for (Node node : disConnectNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getDisConnectNodes().add(new NodePo(node));
            }
        }
        for (Node node : canConnectNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getCanConnectNodes().add(new NodePo(node));
            }
        }
        for (Node node : failNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getFailNodes().add(new NodePo(node));
            }
        }
        for (Node node : uncheckNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getUncheckNodes().add(new NodePo(node));
            }
        }
        for (Node node : connectedNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getCanConnectNodes().add(new NodePo(node));
            }
        }
        return containerPo;
    }


    private DBService getDbService() {
        if (dbService == null) {
            dbService = NulsContext.getServiceBean(DBService.class);
        }
        return dbService;
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
//        getDbService().createArea(NetworkStorageConstant.DB_NAME_NETWORK_NODE);
    }

    private File getStoreageFile() throws IOException {
        Properties properties = ConfigLoader.loadProperties("db_config.properties");
        String path = properties.getProperty("leveldb.datapath", "./data");

        File dir = new File(genAbsolutePath(path));
        File file = new File(dir, NetworkConstant.NODE_FILE_NAME);

        return file;
    }

    private static String genAbsolutePath(String path) {
        String[] paths = path.split("/|\\\\");
        URL resource = ClassLoader.getSystemClassLoader().getResource(".");
        String classPath = resource.getPath();
        File file = new File(classPath);
        String resultPath = null;
        boolean isFileName = false;
        for (String p : paths) {
            if (StringUtils.isBlank(p)) {
                continue;
            }
            if (!isFileName) {
                if ("..".equals(p)) {
                    file = file.getParentFile();
                } else if (".".equals(p)) {
                    continue;
                } else {
                    isFileName = true;
                    resultPath = file.getPath() + File.separator + p;
                }
            } else {
                resultPath += File.separator + p;
            }
        }
        try {
            resultPath = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return resultPath;
    }
}
