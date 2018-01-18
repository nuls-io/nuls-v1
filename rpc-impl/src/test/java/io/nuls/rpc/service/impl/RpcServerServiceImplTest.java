package io.nuls.rpc.service.impl;
import org.junit.Test;


/**
 * @author Niels
 * @date 2018/1/17
 */
public class RpcServerServiceImplTest {

    private RpcServerServiceImpl service = new RpcServerServiceImpl();
    @Test
    public void startServer() throws Exception {
        service.startServer("127.0.0.1",8000,"test");
    }

}