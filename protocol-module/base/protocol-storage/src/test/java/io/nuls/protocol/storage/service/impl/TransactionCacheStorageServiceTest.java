package io.nuls.protocol.storage.service.impl;

import io.nuls.core.tools.log.Log;
import io.nuls.db.module.impl.LevelDbModuleBootstrap;
import io.nuls.kernel.MicroKernelBootstrap;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Transaction;
import io.nuls.protocol.storage.po.TransactionPoTest;
import io.nuls.protocol.storage.service.TransactionCacheStorageService;
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
        for(int i = 0 ; i < 100000 ; i ++) {
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
        assert (count == 100000);
    }
}
