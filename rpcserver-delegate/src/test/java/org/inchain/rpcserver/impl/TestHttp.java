package io.nuls.rpcserver.impl;

import junit.framework.TestCase;
import io.nuls.rpcserver.intf.RpcServerService;
import io.nuls.util.log.Log;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileNotFoundException;

/**
 * Created by Niels on 2017/9/25.
 * nuls.io
 */
public class TestHttp extends TestCase {

    public static ClassPathXmlApplicationContext applicationContext;

    /**
     * start spring
     */
    public static void start() {
        //加载spring环境
        if (null != applicationContext) {
            return;
        }
        Log.info("get application context");
        try {
            applicationContext = new ClassPathXmlApplicationContext();
            applicationContext.getEnvironment().setActiveProfiles();
        } catch (Exception e) {
            Log.error("", e);
            return;
        }
        Log.info("System is started!");
    }

    @Test
    public void test() {
        start();
        RpcServerService service = applicationContext.getBean(RpcServerService.class);

        service.startServer();

        try {
            Thread.sleep(100000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
