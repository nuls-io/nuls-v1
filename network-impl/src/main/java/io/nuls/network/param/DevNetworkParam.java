package io.nuls.network.param;

import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.network.NetworkContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.filter.impl.DefaultMessageFilter;
import io.nuls.network.message.DefaultNetWorkEventHandlerFactory;

import java.net.InetSocketAddress;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class DevNetworkParam extends AbstractNetworkParam {

    private static DevNetworkParam instance;

    private DevNetworkParam() {
        this.maxInCount = NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_NODE_MAX_IN, 50);
        this.maxOutCount =  NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_NODE_MAX_OUT, 10);
        this.port =  NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_PORT_DEV, 8003);
        this.packetMagic =  NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_MAGIC_DEV, 987654323);

        InetSocketAddress address2 = new InetSocketAddress("192.168.1.197", port);
        InetSocketAddress address3 = new InetSocketAddress("192.168.1.248", port);
        InetSocketAddress address1 = new InetSocketAddress("192.168.1.199", port);
        InetSocketAddress address0 = new InetSocketAddress("192.168.1.249", port);
        seedNodes.add(address1);
        seedNodes.add(address2);
        seedNodes.add(address3);
        seedNodes.add(address0);

        this.messageFilter = DefaultMessageFilter.getInstance();
        this.messageHandlerFactory = DefaultNetWorkEventHandlerFactory.getInstance();
    }

    public static synchronized DevNetworkParam get() {
        if (instance == null) {
            instance = new DevNetworkParam();
        }
        return instance;
    }

}
