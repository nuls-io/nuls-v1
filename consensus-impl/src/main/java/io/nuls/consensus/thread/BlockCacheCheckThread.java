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

import io.nuls.consensus.constant.MaintenanceStatus;
import io.nuls.consensus.manager.ConsensusManager;
import io.nuls.consensus.service.intf.BlockService;
import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.context.NulsContext;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/12/19
 */
public class BlockCacheCheckThread implements Runnable {

    private static final long TIME_OUT = 60000;

    private long startTime;
    private BlockService blockService = NulsContext.getServiceBean(BlockService.class);
    private NulsDigestData lastHash;

    public BlockCacheCheckThread() {
        this.startTime = TimeService.currentTimeMillis();
    }

    @Override
    public void run() {
        while (true) {
            try {
//                checkCache();
                Thread.sleep(10000L);
            } catch (Exception e) {
                Log.error(e);
            }
        }

    }

    private void checkCache() {
        Block block = NulsContext.getInstance().getBestBlock();
        if (null == lastHash || !lastHash.equals(block.getHeader().getHash())) {
            this.lastHash = block.getHeader().getHash();
            this.startTime = TimeService.currentTimeMillis();
        } else {
            boolean b = (TimeService.currentTimeMillis() - startTime) > TIME_OUT;
            BlockMaintenanceThread maintenanceThread = BlockMaintenanceThread.getInstance();
            if (b && maintenanceThread.getStatus() == MaintenanceStatus.SUCCESS || maintenanceThread.getStatus() == MaintenanceStatus.FAILED) {
                this.startTime = TimeService.currentTimeMillis();
                maintenanceThread.setStatus(MaintenanceStatus.READY);
                Log.info("set maintenance status to Ready==========================================");
            }
        }

    }
}
