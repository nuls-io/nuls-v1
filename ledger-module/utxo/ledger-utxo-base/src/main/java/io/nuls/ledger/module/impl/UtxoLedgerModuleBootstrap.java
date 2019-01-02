/**
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
 */
package io.nuls.ledger.module.impl;


import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.module.AbstractLedgerModule;
import io.nuls.ledger.task.TotalCoinTask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @desription:
 * @author: PierreLuo
 */
public class UtxoLedgerModuleBootstrap extends AbstractLedgerModule {

    private ScheduledThreadPoolExecutor executorService;

    @Override
    public void init() {

    }

    @Override
    public void start() {
        executorService = TaskManager.createScheduledThreadPool(1,
                new NulsThreadFactory(LedgerConstant.MODULE_ID_LEDGER, "ledger-task-thread-pool"));
        executorService.scheduleAtFixedRate(new TotalCoinTask(), 10, 30 * 60, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public String getInfo() {
        return null;
    }
}
