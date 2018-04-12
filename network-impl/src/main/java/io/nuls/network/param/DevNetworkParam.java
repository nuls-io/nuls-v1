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
package io.nuls.network.param;

import io.nuls.core.context.NulsContext;
import io.nuls.network.NetworkContext;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.filter.impl.DefaultMessageFilter;
import io.nuls.network.message.DefaultNetWorkEventHandlerFactory;

/**
 * @author vivi
 * @date 2017/11/21
 */
public class DevNetworkParam extends AbstractNetworkParam {

    private static DevNetworkParam instance;

    private DevNetworkParam() {
        this.maxInCount = NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_NODE_MAX_IN, 20);
        this.maxOutCount = NetworkContext.getNetworkConfig().getPropValue(NetworkConstant.NETWORK_NODE_MAX_OUT, 10);
        this.port = NulsContext.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SERVER_PORT, 8003);
        this.packetMagic = NulsContext.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_MAGIC, 123456789);

        seedIpList.add("192.168.1.103");
        seedIpList.add("192.168.1.102");
        seedIpList.add("192.168.1.201");
        seedIpList.add("192.168.1.233");

        this.messageFilter = DefaultMessageFilter.getInstance();
        this.messageHandlerFactory = DefaultNetWorkEventHandlerFactory.getInstance();
    }

    public static synchronized DevNetworkParam get() {
        if (instance == null) {
            instance = new DevNetworkParam();
        }
        return instance;
    }

}
