/*
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
 *
 */

package io.nuls.network.netty.module.impl;

import io.nuls.core.tools.network.IpUtil;
import io.nuls.db.constant.DBConstant;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.message.bus.manager.MessageManager;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.netty.conn.NettyServer;
import io.nuls.network.netty.manager.NodeManager;
import io.nuls.network.netty.message.NetworkMessageHandlerPool;
import io.nuls.network.netty.message.filter.MessageFilterChain;
import io.nuls.network.netty.message.filter.impl.MagicNumberFilter;
import io.nuls.network.netty.report.PlatformDepedentReporter;
import io.nuls.network.netty.task.GetNodeVersionTask;
import io.nuls.network.netty.task.NodeDiscoverTask;
import io.nuls.network.netty.task.NodeMaintenanceTask;
import io.nuls.network.netty.task.ShareMineNodeTask;
import io.nuls.network.protocol.message.*;
import io.nuls.protocol.constant.ProtocolConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static io.nuls.network.constant.NetworkConstant.*;

public class NettyNetworkModuleBootstrap extends AbstractNetworkModule {

    private  NetworkParam networkParam;

    private NettyServer nettyServer;
    private NodeManager nodeManager;

    private ScheduledThreadPoolExecutor executorService;

    @Override
    public void init() {
        initNetworkParam();
        initOther();
        initNodes();
    }

    @Override
    public void start() {
        this.waitForDependencyRunning(DBConstant.MODULE_ID_DB, ProtocolConstant.MODULE_ID_PROTOCOL);

        nodeManager.loadDatas();

        nettyServer = new NettyServer(networkParam.getPort());
        nettyServer.startAsSync();


        executorService = TaskManager.createScheduledThreadPool(3,
                new NulsThreadFactory(ProtocolConstant.MODULE_ID_PROTOCOL, "network-task-thread-pool"));

        executorService.scheduleAtFixedRate(new NodeMaintenanceTask(), 1000L, 5000L, TimeUnit.MILLISECONDS);
        executorService.scheduleAtFixedRate(new NodeDiscoverTask(), 10000L, 10000L, TimeUnit.MILLISECONDS);
        executorService.scheduleAtFixedRate(new GetNodeVersionTask(), 2000L, 3000L, TimeUnit.MILLISECONDS);

        TaskManager.createAndRunThread(ProtocolConstant.MODULE_ID_PROTOCOL, "share-mine-node", new ShareMineNodeTask());

        PlatformDepedentReporter reporter = new PlatformDepedentReporter();
        reporter.init();
        reporter.start();
    }

    @Override
    public void shutdown() {
        NetworkMessageHandlerPool.shutdown();

        executorService.shutdown();
        nettyServer.shutdown();
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getInfo() {
        return null;
    }

    private void initNetworkParam() {
        networkParam = NetworkParam.getInstance();
        networkParam.setPort(NulsConfig.MODULES_CONFIG.getCfgValue(NETWORK_SECTION, NETWORK_SERVER_PORT, 8016));
        networkParam.setPacketMagic(NulsConfig.MODULES_CONFIG.getCfgValue(NETWORK_SECTION, NETWORK_MAGIC, 20180712));
        networkParam.setMaxInCount(NulsConfig.MODULES_CONFIG.getCfgValue(NETWORK_SECTION, NETWORK_NODE_MAX_IN, 100));
        networkParam.setMaxOutCount(NulsConfig.MODULES_CONFIG.getCfgValue(NETWORK_SECTION, NETWORK_NODE_MAX_OUT, 20));
        networkParam.setLocalIps(IpUtil.getIps());
        String seedIp = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SEED_IP, "47.254.71.213:8016,47.90.204.15:8016,47.254.152.83:8016,149.129.130.203:8016,211.149.191.152:8016,122.114.0.96:8016");
        List<String> ipList = new ArrayList<>();
        for (String ip : seedIp.split(",")) {
            ipList.add(ip);
        }
        networkParam.setSeedIpList(ipList);
    }

    private void initOther() {
        MagicNumberFilter.getInstance().addMagicNum(NetworkParam.getInstance().getPacketMagic());
        MessageFilterChain.getInstance().addFilter(MagicNumberFilter.getInstance());
        MessageManager.putMessage(HandshakeMessage.class);
        MessageManager.putMessage(GetVersionMessage.class);
        MessageManager.putMessage(VersionMessage.class);
        MessageManager.putMessage(GetNodesMessage.class);
        MessageManager.putMessage(NodesMessage.class);
        MessageManager.putMessage(GetNodesIpMessage.class);
        MessageManager.putMessage(NodesIpMessage.class);
        MessageManager.putMessage(P2PNodeMessage.class);
    }

    private void initNodes() {
        nodeManager = NodeManager.getInstance();

        nodeManager.initNetworkParam(networkParam);
    }
}
