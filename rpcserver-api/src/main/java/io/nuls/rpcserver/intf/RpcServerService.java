package io.nuls.rpcserver.intf;

/**
 * Created by Niels on 2017/9/25.
 * nuls.io
 */
public interface RpcServerService {
    /**
     * start http server，restFul
     */
    void startServer();
    void startServer(String ip,int port,String moduleUrl);

    void shutdown();

    boolean isStarted();

}
