/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
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
import io.nuls.core.utils.network.IpUtil;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkCacheService;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.NodeEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.NetworkService;

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

        Map<String, Node> outNodes = getNetworkService().getNodes();
        boolean exist = false;
        for (Node newNode : event.getEventBody().getNodes()) {
            if (IpUtil.getIps().contains(newNode.getIp())) {
                continue;
            }
            exist = false;
            for (Node outNode : outNodes.values()) {
                if (outNode.getIp().equals(newNode.getIp())) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                newNode.setType(Node.OUT);
                newNode.setStatus(Node.CLOSE);
                newNode.setId(null);
                getNetworkService().addNode(newNode);
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
