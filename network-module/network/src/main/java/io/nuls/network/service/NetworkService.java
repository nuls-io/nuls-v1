/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.network.service;

import io.nuls.network.entity.BroadcastResult;
import io.nuls.network.entity.Node;
import io.nuls.protocol.event.base.BaseEvent;

import java.util.Collection;
import java.util.Map;

/**
 * Created by ln on 2018/5/5.
 */
public interface NetworkService {

    void removeNode(String nodeId);

    Node getNode(String nodeId);

    Collection<Node> getAvailableNodes();

    BroadcastResult sendToAllNode(BaseEvent event, boolean asyn);

    BroadcastResult sendToAllNode(BaseEvent event, String excludeNodeId, boolean asyn);

    BroadcastResult sendToNode(BaseEvent event, String nodeId, boolean asyn);

    BroadcastResult sendToGroup(BaseEvent event, String groupName, boolean asyn);

    BroadcastResult sendToGroup(BaseEvent event, String groupName, String excludeNodeId, boolean asyn);

    boolean reset();

}
