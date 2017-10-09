package io.nuls.rpcserver.impl;

import io.nuls.rpcserver.impl.services.RpcServerServiceImpl;
import junit.framework.TestCase;
import io.nuls.rpcserver.intf.RpcServerService;
import io.nuls.util.log.Log;
import org.junit.Test;

/**
 * Created by Niels on 2017/9/25.
 * nuls.io
 */
public class TestHttp extends TestCase {

    /**
     * start spring
     */
    public static void start() {

        Log.info("System is started!");
    }

    @Test
    public void test() {
        start();
        RpcServerService service = new RpcServerServiceImpl()  ;

        service.startServer("0.0.0.0",8002,"nuls");

        try {
            Thread.sleep(100000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
