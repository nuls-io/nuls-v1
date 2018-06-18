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

package io.nuls.network.module.impl;

import io.nuls.core.tools.network.IpUtil;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.message.bus.manager.MessageManager;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeManager2;
import io.nuls.network.message.filter.MessageFilterChain;
import io.nuls.network.message.filter.impl.MagicNumberFilter;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.protocol.message.*;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.network.constant.NetworkConstant.*;

public class NettyNetworkModuleBootstrap extends AbstractNetworkModule {

    private ConnectionManager connectionManager = ConnectionManager.getInstance();

    private NodeManager2 nodeManager = NodeManager2.getInstance();

    @Override
    public void init() {
        initNetworkParam();
        initOther();
        connectionManager.init();
        nodeManager.init();
    }

    private void initNetworkParam() {
        NetworkParam networkParam = NetworkParam.getInstance();
        networkParam.setPort(NulsConfig.MODULES_CONFIG.getCfgValue(NETWORK_SECTION, NETWORK_SERVER_PORT, 8003));
        networkParam.setPacketMagic(NulsConfig.MODULES_CONFIG.getCfgValue(NETWORK_SECTION, NETWORK_MAGIC, 123456789));
        networkParam.setMaxInCount(NulsConfig.MODULES_CONFIG.getCfgValue(NETWORK_SECTION, NETWORK_NODE_MAX_IN, 30));
        networkParam.setMaxOutCount(NulsConfig.MODULES_CONFIG.getCfgValue(NETWORK_SECTION, NETWORK_NODE_MAX_OUT, 10));
        networkParam.setLocalIps(IpUtil.getIps());
        String seedIp = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SEED_IP, "192.168.1.131:8003");
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

    @Override
    public void start() {
        connectionManager.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        nodeManager.start();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public String getInfo() {
        return null;
    }
}
