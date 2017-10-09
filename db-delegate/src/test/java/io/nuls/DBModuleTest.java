package io.nuls;

import io.nuls.db.DBModule;
import io.nuls.db.DBModuleImpl;
import io.nuls.db.entity.Block;
import io.nuls.db.intf.IBlockStore;
import io.nuls.global.NulsContext;
import io.nuls.util.cfg.ConfigLoader;
import io.nuls.util.log.Log;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.*;


/**
 * Unit test for simple DBModule.
 */
public class DBModuleTest {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    private static ApplicationContext applicationContext;

    /**
     * start spring
     */
    @Before
    public void init() {
        String[] xmls = {"classpath:/applicationContext.xml","classpath:/database-h2.xml"};
        applicationContext = new ClassPathXmlApplicationContext(xmls);
    }

    @org.junit.Test
    public void testDB() {

        IBlockStore blockStore = (IBlockStore) applicationContext.getBean("blockStore");
        Block b = new Block();
        b.setHeight(123L);
        b.setCreatetime(System.currentTimeMillis());
        b.setHash("dfsdfsd");
        blockStore.save(b);

//        long count = blockStore.count();
//        System.out.println(count);
//
//        List<Block> blocks = new ArrayList<>();
//        long start = System.currentTimeMillis();
//
//        for (long i = 0; i < 100; i++) {
//            Block b = new Block();
//            b.setHash("blockkey" + i);
//            b.setHeight(i);
//            b.setCreatetime(System.currentTimeMillis());
//            blocks.add(b);
//        }
//        blockStore.saveBatch(blocks);
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
