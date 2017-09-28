package io.nuls;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import io.nuls.util.log.Log;
import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileNotFoundException;

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
    }
}
