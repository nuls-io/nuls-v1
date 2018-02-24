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
package io.nuls.event.bus.service.intf;

import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.CommonStringEvent;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/8
 */
public interface EventBroadcaster {

    /**
     * broadcast a message that need to be passed
     *
     * @param event
     * @return
     */
    List<String> broadcastHashAndCache(BaseEvent event, boolean needToSelf);


    List<String> broadcastHashAndCache(BaseEvent event, boolean needToSelf, String excludeNodeId);

    boolean broadcastHashAndCacheAysn(BaseEvent event, boolean needToSelf, String excludeNodeId);

    /**
     * broadcast to nodes except "excludeNodeId"
     *
     * @param event
     * @param excludeNodeId
     * @return
     */
    List<String> broadcastAndCache(BaseEvent event, boolean needToSelf, String excludeNodeId);

    /**
     * broadcast msg ,no need to pass the message
     *
     * @param event
     */
    List<String> broadcastAndCache(BaseEvent event, boolean needToSelf);


    boolean broadcastAndCacheAysn(BaseEvent event, boolean needToSelf);

    /**
     * send msg to one node
     * @param event
     * @param nodeId
     */
    boolean sendToNode(BaseEvent event, String nodeId);

    boolean sendToNodeAysn(BaseEvent event, String nodeId);
    boolean sendToGroupAysn(BaseEvent event, String groupName);
}
