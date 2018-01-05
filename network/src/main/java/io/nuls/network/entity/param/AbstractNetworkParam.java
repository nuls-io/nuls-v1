package io.nuls.network.entity.param;

import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.cfg.ConfigLoader;
import io.nuls.core.utils.network.IPUtil;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.message.NetworkEventHandlerFactory;
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

    protected int port;

    protected int packetMagic;

    protected int maxInCount;

    protected int maxOutCount;

    protected NulsMessageFilter messageFilter;

    protected NetworkEventHandlerFactory messageHandlerFactory;

    protected List<InetSocketAddress> seedNodes = new ArrayList<>();

    protected Set<String> localIps = IPUtil.getIps();

    public List<InetSocketAddress> getSeedNodes() {
        return seedNodes;
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

    public NetworkEventHandlerFactory getMessageHandlerFactory() {
        return messageHandlerFactory;
    }

    public Set<String> getLocalIps() {
        return localIps;
    }
}
