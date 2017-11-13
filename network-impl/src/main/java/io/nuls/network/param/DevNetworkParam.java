package io.nuls.network.param;

import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.PeerAddress;
import io.nuls.network.entity.param.NetworkParam;

import java.net.InetSocketAddress;

/**
 * Created by win10 on 2017/11/6.
 */
public class DevNetworkParam extends NetworkParam {

    private static DevNetworkParam instance;

    private DevNetworkParam() {
        this.port = ConfigLoader.getCfgValue(NetworkConstant.Network_Section, NetworkConstant.Network_Port_Dev, 8003);
        this.packetMagic = ConfigLoader.getCfgValue(NetworkConstant.Network_Section, NetworkConstant.Network_Magic_Dev, 987654323L);
        this.maxInCount = ConfigLoader.getCfgValue(NetworkConstant.Network_Section, NetworkConstant.Network_Peer_Max_In, 20);
        this.maxOutCount = ConfigLoader.getCfgValue(NetworkConstant.Network_Section, NetworkConstant.Network_Peer_Max_Out, 10);

        InetSocketAddress address1 = new InetSocketAddress("192.168.1.199", port);
        InetSocketAddress address2 = new InetSocketAddress("192.168.1.201", port);
        InetSocketAddress address3 = new InetSocketAddress("192.168.1.202", port);
        seedPeers.add(address1);
        seedPeers.add(address2);
        seedPeers.add(address3);
    }

    public static synchronized DevNetworkParam get() {
        if (instance == null) {
            instance = new DevNetworkParam();
        }
        return instance;
    }

}
