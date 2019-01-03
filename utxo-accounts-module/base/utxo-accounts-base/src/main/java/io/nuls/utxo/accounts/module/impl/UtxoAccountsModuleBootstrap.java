/*
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
 *
 */
package io.nuls.utxo.accounts.module.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Block;
import io.nuls.kernel.thread.manager.NulsThreadFactory;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.protocol.service.BlockService;
import io.nuls.utxo.accounts.constant.UtxoAccountsConstant;
import io.nuls.utxo.accounts.module.AbstractUtxoAccountsModule;
import io.nuls.utxo.accounts.service.UtxoAccountsService;
import io.nuls.utxo.accounts.storage.service.UtxoAccountsStorageService;
import io.nuls.utxo.accounts.task.UtxoAccountsThread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UtxoAccountsModuleBootstrap extends AbstractUtxoAccountsModule {
    @Override
    public void init() throws Exception {
        Log.info("init utxoAccountsModule");
    }

    @Override
    public void start() {
        Log.info("start utxoAccountsModule");
        //启动进行本地数据一致性判断与回滚
        try {
            UtxoAccountsService utxoAccountsService = NulsContext.getServiceBean(UtxoAccountsService.class);
            UtxoAccountsStorageService utxoAccountsStorageService = NulsContext.getServiceBean(UtxoAccountsStorageService.class);
            BlockService blockService = NulsContext.getServiceBean(BlockService.class);
            long hadSynBlockHeight = utxoAccountsStorageService.getHadSynBlockHeight();
            //启动处理数据一致性
           if(!utxoAccountsService.validateIntegrityBootstrap(hadSynBlockHeight)){
               Log.error("start utxoAccountsModule fail."+hadSynBlockHeight);
               return;
           }
        }catch (Exception e){
            Log.error(e);
            Log.error("start utxoAccountsModule fail.");
            return;

        }
        ScheduledThreadPoolExecutor executor = TaskManager.createScheduledThreadPool(1, new NulsThreadFactory(UtxoAccountsConstant.MODULE_ID_UTXOACCOUNTS, "utxoAccountsThread"));
        executor.scheduleAtFixedRate(NulsContext.getServiceBean(UtxoAccountsThread.class), 15, 1, TimeUnit.SECONDS);

    }

    @Override
    public void shutdown() {
        Log.info("shutdown utxoAccountsModule");
    }

    @Override
    public void destroy() {
        Log.info("destroy utxoAccountsModule");
    }

    @Override
    public String getInfo() {
        Log.info("getInfo utxoAccountsModule");
        return null;
    }
}
