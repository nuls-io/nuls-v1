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
package io.nuls.event.bus.service.impl;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.CommonStringEvent;
import io.nuls.core.utils.spring.lite.annotation.Autowired;
import io.nuls.event.bus.event.CommonDigestEvent;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.event.bus.service.intf.EventBroadcaster;
import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public class EventBroadcasterImpl implements EventBroadcaster {
    @Autowired
    private NetworkService networkService;
    private EventCacheService eventCacheService = EventCacheService.getInstance();
    @Autowired
    private EventBusService eventBusService;

    @Override
    public List<String> broadcastHashAndCache(BaseEvent event, boolean needToSelf) {
        BroadcastResult result = this.networkService.sendToAllNode(new CommonDigestEvent(event.getHash()),false);
        if (needToSelf) {
            eventBusService.publishLocalEvent(event);
        }
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    @Override
    public List<String> broadcastHashAndCache(BaseEvent event, boolean needToSelf, String excludeNodeId) {
        BroadcastResult result = this.networkService.sendToAllNode(new CommonDigestEvent(event.getHash()), excludeNodeId, false);
        if (needToSelf) {
            eventBusService.publishLocalEvent(event);
        }
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }


    private List<String> getNodeIdList(BroadcastResult result) {
        List<String> list = new ArrayList<>();
        if (!result.isSuccess() || result.getBroadcastNodes() == null || result.getBroadcastNodes().isEmpty()) {
            return list;
        }
        for (Node node : result.getBroadcastNodes()) {
            list.add(node.getId());
        }
        return list;
    }

    @Override
    public List<String> broadcastAndCache(BaseEvent event, boolean needToSelf, String excludeNodeId) {
        BroadcastResult result = networkService.sendToAllNode(event, excludeNodeId, false);
        if (needToSelf) {
            eventBusService.publishLocalEvent(event);
        }
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    @Override
    public List<String> broadcastAndCache(BaseEvent event, boolean needToSelf) {
        BroadcastResult result = networkService.sendToAllNode(event, false);
        if (needToSelf) {
            eventBusService.publishLocalEvent(event);
        }
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return getNodeIdList(result);
    }

    @Override
    public boolean sendToNode(BaseEvent event, String nodeId) {
        BroadcastResult result = networkService.sendToNode(event, nodeId, false);
        return result.isSuccess();
    }

    @Override
    public boolean broadcastHashAndCacheAysn(BaseEvent event, boolean needToSelf, String excludeNodeId) {
        BroadcastResult result = this.networkService.sendToAllNode(new CommonDigestEvent(event.getHash()), excludeNodeId, true);
        if (needToSelf) {
            eventBusService.publishLocalEvent(event);
        }
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return result.isSuccess();
    }

    @Override
    public boolean broadcastAndCacheAysn(BaseEvent event, boolean needToSelf) {
        BroadcastResult result = networkService.sendToAllNode(event, true);
        if (needToSelf) {
            eventBusService.publishLocalEvent(event);
        }
        if (result.isSuccess()) {
            eventCacheService.cacheSendedEvent(event);
        }
        return result.isSuccess();
    }

    @Override
    public boolean sendToNodeAysn(BaseEvent event, String nodeId) {
        BroadcastResult result = networkService.sendToNode(event, nodeId, true);
        return result.isSuccess();
    }

    @Override
    public boolean sendToGroupAysn(BaseEvent event, String groupName) {
        BroadcastResult result = networkService.sendToGroup(event, groupName, true);
        return result.isSuccess();
    }

    @Override
    public boolean publishToLocal(BaseEvent event) {
        this.eventBusService.publishLocalEvent(event);
        return true;
    }

}
