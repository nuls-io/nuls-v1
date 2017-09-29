package io.nuls;

import io.nuls.db.entity.Block;
import io.nuls.db.intf.IStoreService;
import junit.framework.TestCase;
import io.nuls.util.log.Log;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileNotFoundException;
import java.util.Random;

/**
 * Unit test for simple DBModule.
 */
public class DBModuleTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */

    public static ClassPathXmlApplicationContext applicationContext;

    /**
     * start spring
     */
    @Before
    public static void init() {
        //加载spring环境
        if (null != applicationContext) {
            return;
        }
        Log.info("get application context");
        try {
            String[] xmls = new String[2];
            xmls[0] = "classpath:/applicationContext.xml";
            xmls[1] = "classpath:/database-h2.xml";
            applicationContext = new ClassPathXmlApplicationContext(xmls);
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                Log.error("There is not applicationContext.xml in classpath!", e);
            } else {
                Log.error("", e);
            }
            return;
        }

        Log.info("System is started!");

    }

    @org.junit.Test
    public void testDB() {

        init();

        IStoreService<Block, String> blockStore = (IStoreService) applicationContext.getBean("blockStoreService");
        blockStore.truncate();

        try {
            for (int j = 0; j < 10; j++) {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {

                                long start = System.currentTimeMillis();

                                for (long i = 0; i < 100000; i++) {
                                    Block b = new Block();
                                    b.setHash("blockkey" + i);
                                    b.setHeight(i);
                                    b.setCreatetime(System.currentTimeMillis());
                                    blockStore.save(b);
                                }


                                long end = System.currentTimeMillis();

                                System.out.println("-----------------use:" + (start - end));
                            }
                        }
                ).start();

            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
