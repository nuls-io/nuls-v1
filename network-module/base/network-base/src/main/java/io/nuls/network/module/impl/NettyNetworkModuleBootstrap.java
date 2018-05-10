package io.nuls.network.module.impl;

import io.nuls.core.tools.network.IpUtil;
import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.message.filter.MessageFilterChain;
import io.nuls.network.message.filter.impl.MagicNumberFilter;
import io.nuls.network.module.AbstractNetworkModule;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.network.constant.NetworkConstant.*;

public class NettyNetworkModuleBootstrap extends AbstractNetworkModule {

    private ConnectionManager connectionManager = ConnectionManager.getInstance();

    private NodeManager nodeManager = NodeManager.getInstance();

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
