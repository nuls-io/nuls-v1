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
