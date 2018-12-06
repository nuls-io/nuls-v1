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

import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.message.GetVersionMessage;
import io.nuls.network.protocol.message.NetworkMessageBody;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author vivi
 */
public class NewNodeDiscoverHandler implements Runnable {

    private static NewNodeDiscoverHandler instance = new NewNodeDiscoverHandler();

    private NewNodeDiscoverHandler() {
    }

    public static NewNodeDiscoverHandler getInstance() {
        return instance;
    }

    private NetworkParam networkParam = NetworkParam.getInstance();

    private NodeManager nodesManager = NodeManager.getInstance();

    private BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();

    private boolean isFirstRunning = false;

    public void start() {
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "NetworkNodeDiscover", this);
    }

    /**
     * 获取种子节点
     */
    public List<Node> getSeedNodes() {
        List seedNodes = new ArrayList<>();
        for (String seedIp : networkParam.getSeedIpList()) {
            String[] ipPort = seedIp.split(":");
            seedNodes.add(new Node(ipPort[0], Integer.parseInt(ipPort[1]), Integer.parseInt(ipPort[1]), Node.OUT));
        }
        return seedNodes;
    }

    /**
     * 节点发现需要时刻关注当前连接的节点的状态，尽可能多连接到更多节点
     * 1. 首次启动后，当已连接成功的节点稳定时，向对方询问一次可连接的节点列表（10秒后，连接成功的节点数量没有变化）
     * 2. 检测主动连接成功的节点，小于2个时，就立刻尝试主动连接种子节点
     * 3. 主动连接数超过10个时，就尝试和种子节点断开，最终只保留不超过2个种子节点处于连接状态
     * 4. 如果不满足最大连接数时，从节点池里查找可连接的节点，尝试连接
     */

    @Override
    public void run() {
        //如果是首次启动，等节点稳定后
        int lastCount = 0;
        if (!isFirstRunning) {

        } else {

        }
    }


    public void setFirstRunning(boolean b) {
        isFirstRunning = b;
    }
}
