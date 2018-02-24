/**
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

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NodeEventHandler implements NetWorkEventHandler {

    private static final NodeEventHandler INSTANCE = new NodeEventHandler();

    private NetworkService networkService;

    private NetworkCacheService cacheService;

    private NodeEventHandler() {
        cacheService = NetworkCacheService.getInstance();
    }

    public static NodeEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseEvent networkEvent, Node node) {
        NodeEvent event = (NodeEvent) networkEvent;

//        String key = event.getHeader().getEventType() + "-" + node.getIp();
//        if (cacheService.existEvent(key)) {
//            networkService.removeNode(node.getId());
//            return null;
//        }
//        cacheService.putEvent(key, event, false);

        for (Node newNode : event.getEventBody().getNodes()) {
            newNode.setType(Node.OUT);
            newNode.setStatus(Node.WAIT);
            getNetworkService().addNodeToGroup(NetworkConstant.NETWORK_NODE_OUT_GROUP, newNode);
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
