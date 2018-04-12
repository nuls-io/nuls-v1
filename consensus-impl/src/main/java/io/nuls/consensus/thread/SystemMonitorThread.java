/*
 *
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
package io.nuls.consensus.thread;

import io.nuls.consensus.service.intf.SystemService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;

import java.util.List;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class SystemMonitorThread implements Runnable {

    private static final long TIME_OUT = 300000;

    private long startTime;

    private NetworkService networkService = NulsContext.getServiceBean(NetworkService.class);
    private SystemService systemService = NulsContext.getServiceBean(SystemService.class);

    private NulsDigestData lastHash;

    public SystemMonitorThread() {
        this.startTime = TimeService.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            doMonitor();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void doMonitor() {

        checkLocalBlockGrow();

        checkNetworkIsDisconnected();

    }

    private void checkNetworkIsDisconnected() {
        List<Node> nodes = networkService.getAvailableNodes();
        if (nodes == null || nodes.size() == 0) {
            systemService.resetSystem("the number of network connection nodes is 0");
        }
    }

    /*
     * Check if the local block grows
     * 检查本地区块是否增长
     */
    private void checkLocalBlockGrow() {
        Block block = NulsContext.getInstance().getBestBlock();
        if (null == lastHash || !lastHash.equals(block.getHeader().getHash())) {
            this.lastHash = block.getHeader().getHash();
            this.startTime = TimeService.currentTimeMillis();
        } else {
            boolean b = (TimeService.currentTimeMillis() - startTime) > TIME_OUT;
            if (b) {
                this.startTime = TimeService.currentTimeMillis();

                systemService.resetSystem("the local block does not grow");
            }
        }

    }
}
