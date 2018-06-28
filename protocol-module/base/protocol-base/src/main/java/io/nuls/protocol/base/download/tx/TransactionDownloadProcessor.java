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

package io.nuls.protocol.base.download.tx;

import io.nuls.consensus.service.ConsensusService;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.cache.TemporaryCacheManager;
import io.nuls.protocol.service.TransactionService;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Niels Wang
 * @date: 2018/6/28
 */
public class TransactionDownloadProcessor implements Runnable {

    private static final TransactionDownloadProcessor INSTANCE = new TransactionDownloadProcessor();

    private BlockingQueue<TransactionContainer> queue = new LinkedBlockingDeque<>();

    private TemporaryCacheManager temporaryCacheManager = TemporaryCacheManager.getInstance();

    private ConsensusService consensusService = NulsContext.getServiceBean(ConsensusService.class);
    private TransactionService transactionService = NulsContext.getServiceBean(TransactionService.class);

    private AtomicInteger count = new AtomicInteger(0);

    private TransactionDownloadProcessor() {
    }

    public static TransactionDownloadProcessor getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        while (true) {
            try {
                process();
            } catch (Exception e) {
                Log.error(e);
            }
        }
    }

    private void process() {

        Transaction tx;
        try {
            TransactionContainer container = queue.take();
            if (null == container || container.getFuture() == null) {
                return;
            }
            tx = container.getFuture().get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.error(e);
            return;
        }

        if (null == tx || tx.isSystemTx()) {
            return;
        }
        int size = count.incrementAndGet();
        if (size % 100 == 0) {
            Log.error("tx count:::::" + size);
        }
        try {
            consensusService.newTx(tx);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public void offer(TransactionContainer container) {
        queue.offer(container);
    }
}
