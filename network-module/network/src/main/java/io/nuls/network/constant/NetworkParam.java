package io.nuls.network.constant;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkParam {

    private static NetworkParam instance = new NetworkParam();

    public static NetworkParam getInstance() {
        return instance;
    }

    private NetworkParam() {
    }

    private int port;

    private long packetMagic;

    private int maxInCount;

    private int maxOutCount;

    private Set<String> localIps;

    private List<String> seedIpList;

    private Map<String, Long> ipMap = new ConcurrentHashMap<>();

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getPacketMagic() {
        return packetMagic;
    }

    public void setPacketMagic(long packetMagic) {
        this.packetMagic = packetMagic;
    }

    public int getMaxInCount() {
        return maxInCount;
    }

    public void setMaxInCount(int maxInCount) {
        this.maxInCount = maxInCount;
    }

    public int getMaxOutCount() {
        return maxOutCount;
    }

    public void setMaxOutCount(int maxOutCount) {
        this.maxOutCount = maxOutCount;
    }

    public Set<String> getLocalIps() {
        return localIps;
    }

    public void setLocalIps(Set<String> localIps) {
        this.localIps = localIps;
    }

    public List<String> getSeedIpList() {
        return seedIpList;
    }

    public void setSeedIpList(List<String> seedIpList) {
        this.seedIpList = seedIpList;
    }

    public Map<String, Long> getIpMap() {
        return ipMap;
    }

}
