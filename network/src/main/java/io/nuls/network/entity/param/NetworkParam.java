/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.entity.param;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.utils.network.IpUtil;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.message.NetworkEventHandlerFactory;
import io.nuls.network.message.filter.NulsMessageFilter;
import io.nuls.protocol.context.NulsContext;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author vivi
 * @date 2017/11/6
 */
public class NetworkParam {

    private static NetworkParam instance = new NetworkParam();

    private NetworkParam() {
        this.port = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SERVER_PORT, 8003);
        this.packetMagic = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_MAGIC, 123456789);
        this.maxInCount = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_NODE_MAX_IN, 50);
        this.maxOutCount = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_NODE_MAX_OUT, 10);

        this.localIps = IpUtil.getIps();
        this.seedIpList = new ArrayList<>();
        String seedIp = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SEED_IP, "192.168.1.131:8003");
        for (String str : seedIp.split(",")) {
            this.seedIpList.add(str);
        }
    }

    public static NetworkParam getInstance() {
        return instance;
    }

    private int port;

    private int packetMagic;

    private int maxInCount;

    private int maxOutCount;

    private NulsMessageFilter messageFilter;

    private NetworkEventHandlerFactory messageHandlerFactory;

    private List<String> seedIpList = new ArrayList<>();

    private Set<String> localIps = IpUtil.getIps();

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPacketMagic() {
        return packetMagic;
    }

    public void setPacketMagic(int packetMagic) {
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

    public NulsMessageFilter getMessageFilter() {
        return messageFilter;
    }

    public void setMessageFilter(NulsMessageFilter messageFilter) {
        this.messageFilter = messageFilter;
    }

    public NetworkEventHandlerFactory getMessageHandlerFactory() {
        return messageHandlerFactory;
    }

    public void setMessageHandlerFactory(NetworkEventHandlerFactory messageHandlerFactory) {
        this.messageHandlerFactory = messageHandlerFactory;
    }

    public List<String> getSeedIpList() {
        return seedIpList;
    }

    public void setSeedIpList(List<String> seedIpList) {
        this.seedIpList = seedIpList;
    }

    public Set<String> getLocalIps() {
        return localIps;
    }

    public void setLocalIps(Set<String> localIps) {
        this.localIps = localIps;
    }
}
