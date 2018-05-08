/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.manager;

import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class NodeDiscoverHandler implements Runnable {

    private NetworkParam network = NetworkParam.getInstance();

    @Autowired
    private NodeManager nodesManager;

    @Autowired
    private BroadcastHandler broadcastHandler;

    private boolean running = false;

    public void start() {
        running = true;
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "NetworkNodeDiscover", this);
    }

    /**
     * Inquire more of the other nodes to the connected nodes
     *
     * @param size
     */
    public void findOtherNode(int size) {
       // GetNodeEvent event = new GetNodeEvent(size);
        List<Node> nodeList = new ArrayList<>(nodesManager.getAvailableNodes());
        Collections.shuffle(nodeList);
        for (int i = 0; i < nodeList.size(); i++) {
            if (i == 2) {
                break;
            }
            Node node = nodeList.get(i);
            broadcastHandler.broadcastToNode(null, node, true);
        }
    }

    @Override
    public void run() {

    }
}
