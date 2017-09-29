package io.nuls;

import io.nuls.db.DBModule;
import io.nuls.db.DBModuleImpl;
import io.nuls.db.entity.Block;
import io.nuls.db.intf.IBlockStore;
import io.nuls.global.NulsContext;
import io.nuls.util.cfg.ConfigLoader;
import io.nuls.util.log.Log;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.Properties;


/**
 * Unit test for simple DBModule.
 */
public class DBModuleTest {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */


    private DBModule dbModule;
    /**
     * start spring
     */
    public void init() {
        NulsContext.setApplicationContext(new ClassPathXmlApplicationContext("classpath:/applicationContext.xml"));
        dbModule = (DBModule) NulsContext.getApplicationContext().getBean("dbModule");
        dbModule.init(null);
    }

    @org.junit.Test
    public void testDB() {

        init();

        IBlockStore blockStore = (IBlockStore) NulsContext.getApplicationContext().getBean("blockStore");

        long count = blockStore.count();
        System.out.println(count);
//        long start = System.currentTimeMillis();
//
//        for (long i = 0; i < 100000; i++) {
//            Block b = new Block();
//            b.setHash("blockkey" + i);
//            b.setHeight(i);
//            b.setCreatetime(System.currentTimeMillis());
//            try{
//                blockStore.save(b);
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//
//
//        long end = System.currentTimeMillis();
//
//        System.out.println("-----------------use:" + (start - end));


//        try {
//            for (int j = 0; j < 20; j++) {
//                new Thread(
//                        new Runnable() {
//                            @Override
//                            public void run() {
//
//                                long start = System.currentTimeMillis();
//
//                                for (long i = 0; i < 100000; i++) {
//                                    Block b = new Block();
//                                    b.setHash("blockkey" + i);
//                                    b.setHeight(i);
//                                    b.setCreatetime(System.currentTimeMillis());
//                                    try{
//                                        blockStore.save(b);
//                                    }catch(Exception e){
//                                        e.printStackTrace();
//                                    }
//                                }
//
//
//                                long end = System.currentTimeMillis();
//
//                                System.out.println("-----------------use:" + (start - end));
//                            }
//                        }
//                ).start();
//            }
//            Thread.sleep(30000l);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
