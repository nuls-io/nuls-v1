package io.nuls.network.entity.param;

import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.network.IPUtil;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.AbstractNetWorkDataHandlerFactory;
import io.nuls.network.message.filter.NulsMessageFilter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author vivi
 * @date 2017/11/6
 */
public abstract class AbstractNetworkParam {

    public AbstractNetworkParam() {
        this.maxInCount = ConfigLoader.getPropValue(NetworkConstant.NETWORK_PEER_MAX_IN, 50);
        this.maxOutCount = ConfigLoader.getPropValue(NetworkConstant.NETWORK_PEER_MAX_OUT, 10);
    }

    protected int port;

    protected int packetMagic;

    protected int maxInCount;

    protected int maxOutCount;

    protected NulsMessageFilter messageFilter;

    protected AbstractNetWorkDataHandlerFactory messageHandlerFactory;

    protected List<InetSocketAddress> seedPeers = new ArrayList<>();

    protected Set<String> localIps = IPUtil.getIps();

    public List<InetSocketAddress> getSeedPeers() {
        return seedPeers;
    }

    public int port() {
        return port;
    }

    public int packetMagic() {
        return packetMagic;
    }

    public int maxInCount() {
        return maxInCount;
    }

    public void maxInCount(int count) {
        this.maxInCount = count;
    }

    public int maxOutCount() {
        return maxOutCount;
    }

    public void maxOutCount(int count) {
        this.maxOutCount = count;
    }

    public void setMessageFilter(NulsMessageFilter messageFilter) {
        this.messageFilter = messageFilter;
    }

    public NulsMessageFilter getMessageFilter() {
        return messageFilter;
    }

    public AbstractNetWorkDataHandlerFactory getMessageHandlerFactory() {
        return messageHandlerFactory;
    }

    public Set<String> getLocalIps() {
        return localIps;
    }
}
