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
package io.nuls.consensus.poc.storage.service;

import io.nuls.core.tools.log.Log;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Transaction;
import org.junit.Before;
import org.junit.Test;

public class TransactionCacheStorageServiceTest {

    TransactionCacheStorageService service = null;

    @Before
    public void init() {
        try {
            MicroKernelBootstrap mk = MicroKernelBootstrap.getInstance();
            mk.init();
            mk.start();

            LevelDbModuleBootstrap bootstrap = new LevelDbModuleBootstrap();
            bootstrap.init();
            bootstrap.start();

            service = NulsContext.getServiceBean(TransactionCacheStorageService.class);
        } catch (Exception e) {
            Log.error(e);
        }
    }

    @Test
    public void test() {
        Transaction tx = null;

        long time = System.currentTimeMillis();
        for(int i = 0 ; i < 1000000 ; i ++) {
            tx = new TransactionPoTest(1);
            tx.setTime(i);
            tx.setRemark("asldfjsaldfjsldjfoijioj222fsdafasdfasdfasdfasdfasdfasdfsadfsa".getBytes());

            boolean success = service.putTx(tx);
            assert(success);
        }
        System.out.println("新增耗时：" + (System.currentTimeMillis() - time) + " ms");

        time = System.currentTimeMillis();

        int count = 0;
        while((tx = service.pollTx()) != null) {
            count++;
        }
        System.out.println(count);
        System.out.println("出队耗时：" + (System.currentTimeMillis() - time) + " ms");
        assert (count == 1000000);
    }
}
