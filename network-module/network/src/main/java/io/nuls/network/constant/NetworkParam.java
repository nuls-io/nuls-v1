/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
