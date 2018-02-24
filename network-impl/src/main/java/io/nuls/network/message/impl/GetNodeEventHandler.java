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
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkCacheService;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.entity.GetNodeEvent;
import io.nuls.network.message.entity.NodeEvent;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class GetNodeEventHandler implements NetWorkEventHandler {

    private static final GetNodeEventHandler INSTANCE = new GetNodeEventHandler();

    private NetworkService networkService;

    private NetworkCacheService cacheService;

    private GetNodeEventHandler() {
        cacheService = NetworkCacheService.getInstance();
    }

    public static GetNodeEventHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public NetworkEventResult process(BaseEvent event, Node node) {
        GetNodeEvent getNodeEvent = (GetNodeEvent) event;

        String key = event.getHeader().getEventType() + "-" + node.getIp();
        if (cacheService.existEvent(key)) {
            getNetworkService().removeNode(node.getId());
            return null;
        }
        cacheService.putEvent(key, event, false);

        List<Node> list = getAvailableNodes(getNodeEvent.getLength(), node.getId());
        NodeEvent replyEvent = new NodeEvent(list);
        return new NetworkEventResult(true, replyEvent);
    }


    private List<Node> getAvailableNodes(int length, String nodeId) {
        List<Node> nodes = new ArrayList<>();
        int count = 0;
        for (Node node : getNetworkService().getAvailableNodes()) {
            if (node.getId().equals(nodeId)) {
                continue;
            }
            nodes.add(node);
            count++;
            if (count == length) {
                break;
            }
        }
        if(nodes.isEmpty()) {
            Node node1 = new Node();
            node1.setIp("192.168.1.111");
            node1.setPort(8003);
            node1.setMagicNumber(123456789);
            nodes.add(node1);
        }
        return nodes;
    }

    private NetworkService getNetworkService() {
        if (networkService == null) {
            networkService = NulsContext.getServiceBean(NetworkService.class);
        }
        return networkService;
    }

}
