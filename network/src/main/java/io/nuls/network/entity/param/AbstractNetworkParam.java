/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.entity.param;

import io.nuls.core.utils.network.IpUtil;
import io.nuls.network.entity.Node;
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
    public enum NetworkType {
        DEV,
        TEST,
        MAIN
    }

    protected NetworkType type;

    protected int port;

    protected int packetMagic;

    protected int maxInCount;

    protected int maxOutCount;

    protected NulsMessageFilter messageFilter;

    protected NetworkEventHandlerFactory messageHandlerFactory;

    protected List<String> seedIpList = new ArrayList<>();

    protected Set<String> localIps = IpUtil.getIps();

    public List<String> getSeedIpList() {
        return seedIpList;
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
