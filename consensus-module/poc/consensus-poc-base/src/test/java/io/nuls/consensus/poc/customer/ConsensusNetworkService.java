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

package io.nuls.consensus.poc.customer;

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.BroadcastResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.service.NetworkService;

import java.util.Collection;
import java.util.Map;

/**
 * Created by ln on 2018/5/9.
 */
public class ConsensusNetworkService implements NetworkService {
    @Override
    public void removeNode(String nodeId) {

    }

    @Override
    public Node getNode(String nodeId) {
        return null;
    }

    @Override
    public Map<String, Node> getNodes() {
        return null;
    }

    @Override
    public Collection<Node> getAvailableNodes() {
        return null;
    }

    @Override
    public NodeGroup getNodeGroup(String groupName) {
        // todo auto-generated method stub
        return null;
    }

    @Override
    public BroadcastResult sendToAllNode(BaseNulsData event, boolean asyn) {
        return null;
    }

    @Override
    public BroadcastResult sendToAllNode(BaseNulsData event, Node excludeNode, boolean asyn) {
        return null;
    }

    @Override
    public BroadcastResult sendToNode(BaseNulsData event, Node node, boolean asyn) {
        return null;
    }

    @Override
    public BroadcastResult sendToGroup(BaseNulsData event, String groupName, boolean asyn) {
        return null;
    }

    @Override
    public BroadcastResult sendToGroup(BaseNulsData event, String groupName, Node excludeNode, boolean asyn) {
        return null;
    }

    @Override
    public void reset() {
    }

    @Override
    public NetworkParam getNetworkParam() {
        return null;
    }
}