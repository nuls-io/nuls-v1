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
package io.nuls.network.service;

import io.nuls.core.event.BaseEvent;
import io.nuls.network.entity.BroadcastResult;

/**
 * @author vivi
 * @date 2017/11/21
 */
public interface Broadcaster {

    BroadcastResult broadcast(BaseEvent event);

    BroadcastResult broadcast(BaseEvent event, String excludeNodeId);

    BroadcastResult broadcast(byte[] data);

    BroadcastResult broadcast(byte[] data, String excludeNodeId);

//    BroadcastResult broadcastSync(BaseNulsEvent event);
//
//    BroadcastResult broadcastSync(BaseNulsEvent event, String excludeNodeId);
//
//    BroadcastResult broadcastSync(byte[] data);
//
//    BroadcastResult broadcastSync(byte[] data, String excludeNodeId);

    BroadcastResult broadcastToNode(BaseEvent event, String nodeId);

    BroadcastResult broadcastToNode(byte[] data, String nodeId);

    BroadcastResult broadcastToGroup(BaseEvent event, String groupName);

    BroadcastResult broadcastToGroup(String area, BaseEvent event, String groupName);

    BroadcastResult broadcastToGroup(BaseEvent event, String groupName, String excludeNodeId);

    BroadcastResult broadcastToGroup( BaseEvent event, String area, String groupName, String excludeNodeId);

    BroadcastResult broadcastToGroup(byte[] data, String groupName);

    BroadcastResult broadcastToGroup(String area, byte[] data,  String groupName);

    BroadcastResult broadcastToGroup(byte[] data, String groupName, String excludeNodeId);

    BroadcastResult broadcastToGroup(String area, byte[] data, String groupName, String excludeNodeId);

//    BroadcastResult broadcastToGroupSync(BaseNulsEvent event, String groupName);
//
//    BroadcastResult broadcastToGroupSync(BaseNulsEvent event, String groupName, String excludeNodeId);
//
//    BroadcastResult broadcastToGroupSync(byte[] data, String groupName);
//
//    BroadcastResult broadcastToGroupSync(byte[] data, String groupName, String excludeNodeId);

}
