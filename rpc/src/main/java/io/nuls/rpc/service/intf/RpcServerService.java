package io.nuls.rpc.service.intf;

/**
 *
 * @author Niels
 * @date 2017/9/25
 */
public interface RpcServerService {
    /**
     * start http server，restFul
     */
    void startServer(String ip, int port, String moduleUrl);

    void shutdown();

    boolean isStarted();
}
