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

package io.nuls.consensus.poc.process;

import io.nuls.account.service.AccountService;
import io.nuls.consensus.poc.model.RewardStatisticsParam;
import io.nuls.consensus.poc.service.impl.PocRewardCacheService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.model.Block;
import io.nuls.protocol.constant.ProtocolConstant;
import io.nuls.protocol.service.BlockService;
import io.nuls.protocol.service.DownloadService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author: Niels Wang
 * @date: 2018/5/24
 */
@Component
public class RewardStatisticsProcess {

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private AccountService accountService;

    private static BlockingQueue<RewardStatisticsParam> queue = new LinkedBlockingDeque<>(100);

    @Autowired
    private PocRewardCacheService service;

    public void initProcess() {
        if (downloadService.isDownloadSuccess().isFailed()) {
            try {
                Thread.sleep(ProtocolConstant.BLOCK_TIME_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Log.error(e);
            }
            initProcess();
            return;
        }
        service.initCache();
        while (true) {
            try {
                RewardStatisticsParam param = queue.take();
                if (param.getType() == 0) {
                    service.addBlock(param.getBlock());
                } else if (param.getType() == 1) {
                    service.rollback(param.getBlock());
                }
            } catch (Exception e) {
                Log.error(e);
            }
        }

    }

    public void doProcess() {
        service.calcRewards();
    }


    public static void addBlock(Block block) {
        if (NulsContext.getServiceBean(DownloadService.class).isDownloadSuccess().isFailed()) {
            return;
        }
        queue.add(new RewardStatisticsParam(0, block));
    }

    public static void rollbackBlock(Block block) {
        queue.add(new RewardStatisticsParam(1, block));
    }

}
