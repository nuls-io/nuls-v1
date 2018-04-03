/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.message.impl;

import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkCacheService;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.NodeEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.NetworkService;
import sun.nio.ch.Net;

import java.util.List;
import java.util.Map;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NodeEventHandler implements NetWorkEventHandler {

    private static final NodeEventHandler INSTANCE = new NodeEventHandler();

    private NetworkService networkService;

    private NetworkCacheService cacheService = NetworkCacheService.getInstance();

    private NodeEventHandler() {

    }

    public static NodeEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseEvent networkEvent, Node node) {
        NodeEvent event = (NodeEvent) networkEvent;

        Map<String, Node> outNodes = networkService.getNodeGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP).getNodes();
        boolean exist = false;
        for (Node newNode : event.getEventBody().getNodes()) {
            exist = false;
            for (Node outNode : outNodes.values()) {
                if (outNode.getIp().equals(node.getIp())) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                newNode.setType(Node.OUT);
                newNode.setStatus(Node.WAIT);
                getNetworkService().addNodeToGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP, newNode);
            }
        }
        return null;
    }

    private NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getServiceBean(NetworkService.class);
        }
        return networkService;
    }
}
