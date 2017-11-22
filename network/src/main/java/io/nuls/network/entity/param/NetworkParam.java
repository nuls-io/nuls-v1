package io.nuls.network.entity.param;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.utils.network.IPUtil;
import io.nuls.network.message.NetWorkMessageHandlerFactory;
import io.nuls.network.message.messageFilter.NulsMessageFilter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author vivi
 * @date 2017.11.01
 */
public abstract class NetworkParam {

    protected int port;

    protected int packetMagic;

    protected int maxInCount;

    protected int maxOutCount;

    protected Block bestBlock;

    protected NulsMessageFilter messageFilter;

    protected NetWorkMessageHandlerFactory messageHandlerFactory;

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

    public int maxOutCount() {
        return maxOutCount;
    }

    public void setBestBlock(Block bestBlock) {
        this.bestBlock = bestBlock;
    }

    public Block getBestBlock() {
        return bestBlock;
    }

    public void setMessageFilter(NulsMessageFilter messageFilter) {
        this.messageFilter = messageFilter;
    }

    public NulsMessageFilter getMessageFilter() {
        return messageFilter;
    }

    public NetWorkMessageHandlerFactory getMessageHandlerFactory() {
        return messageHandlerFactory;
    }

    public Set<String> getLocalIps() {
        return localIps;
    }
}
