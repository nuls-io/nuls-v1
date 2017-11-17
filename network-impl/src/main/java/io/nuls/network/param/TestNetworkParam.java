package io.nuls.network.param;

import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.PeerAddress;
import io.nuls.network.entity.param.NetworkParam;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by win10 on 2017/11/6.
 */
public class TestNetworkParam extends NetworkParam {

    private static TestNetworkParam instance;

    private TestNetworkParam() {
        this.port = ConfigLoader.getPropValue(NetworkConstant.Network_Port_Test, 8002);
        this.packetMagic = ConfigLoader.getPropValue(NetworkConstant.Network_Magic_Test, 987654322);
        this.maxInCount = ConfigLoader.getPropValue(NetworkConstant.Network_Peer_Max_In, 20);
        this.maxOutCount = ConfigLoader.getPropValue(NetworkConstant.Network_Peer_Max_Out, 10);

        InetSocketAddress address1 = new InetSocketAddress("192.168.1.156", port);
        InetSocketAddress address2 = new InetSocketAddress("192.168.1.157", port);
        InetSocketAddress address3 = new InetSocketAddress("192.168.1.158", port);
        seedPeers.add(address1);
        seedPeers.add(address2);
        seedPeers.add(address3);
    }

    public static synchronized TestNetworkParam get() {
        if (instance == null) {
            instance = new TestNetworkParam();
        }
        return instance;
    }

}
