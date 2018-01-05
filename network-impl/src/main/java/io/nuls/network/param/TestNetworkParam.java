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
public class TestNetworkParam extends AbstractNetworkParam {

    private static TestNetworkParam instance;

    private TestNetworkParam() {
        this.maxInCount = NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_PEER_MAX_IN, 50);
        this.maxOutCount =  NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_PEER_MAX_OUT, 10);
        this.port = NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_PORT_TEST, 8002);
        this.packetMagic = NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_MAGIC_TEST, 987654322);

        InetSocketAddress address1 = new InetSocketAddress("192.168.1.156", port);
        InetSocketAddress address2 = new InetSocketAddress("192.168.1.157", port);
        InetSocketAddress address3 = new InetSocketAddress("192.168.1.158", port);
        seedPeers.add(address1);
        seedPeers.add(address2);
        seedPeers.add(address3);

        this.messageFilter = DefaultMessageFilter.getInstance();
        this.messageHandlerFactory = DefaultNetWorkEventHandlerFactory.getInstance();
    }

    public static synchronized TestNetworkParam get() {
        if (instance == null) {
            instance = new TestNetworkParam();
        }
        return instance;
    }

}
