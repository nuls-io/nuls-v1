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

import java.util.*;

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

//        String key = event.getHeader().getEventType() + "-" + node.getIp();
//        if (cacheService.existEvent(key)) {
//            getNetworkService().removeNode(node.getId());
//            return null;
//        }
//        cacheService.putEvent(key, event, false);

        List<Node> list = getAvailableNodes(getNodeEvent.getLength(), node.getIp());
        NodeEvent replyEvent = new NodeEvent(list);
        return new NetworkEventResult(true, replyEvent);
    }

    private List<Node> getAvailableNodes(int length, String nodeIp) {
        List<Node> nodes = new ArrayList<>();
        List<Node> availableNodes = getNetworkService().getAvailableNodes();
        Collections.shuffle(availableNodes);
        Set<String> ipSet = new HashSet<>();
        ipSet.add(nodeIp);
        for (Node node : availableNodes) {
            if (ipSet.contains(node.getIp())) {
                continue;
            }
            if (node.getSeverPort() == null || node.getSeverPort() == 0) {
                continue;
            }
            Node newNode = new Node();
            newNode.setIp(node.getIp());
            newNode.setPort(node.getSeverPort());
            newNode.setSeverPort(node.getSeverPort());
            ipSet.add(node.getIp());
            nodes.add(newNode);
            if (nodes.size() == length) {
                break;
            }
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
