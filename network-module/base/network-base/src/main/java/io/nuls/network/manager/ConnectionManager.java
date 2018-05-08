package io.nuls.network.manager;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.base.NetworkParam;
import io.nuls.network.connection.netty.NettyClient;
import io.nuls.network.connection.netty.NettyServer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.service.NetworkService;

@Component
public class ConnectionManager {

    private NetworkParam network = NetworkParam.getInstance();

    private NettyServer nettyServer;

    @Autowired
    private NetworkService networkService;

    public void init() {
        nettyServer = new NettyServer(network.getPort());
        nettyServer.init();
//        eventBusService = NulsContext.getServiceBean(EventBusService.class);
//        messageHandlerFactory = network.getMessageHandlerFactory();
    }

    public void start() {
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "node connection", new Runnable() {
            @Override
            public void run() {
                try {
                    nettyServer.start();
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
        }, false);
    }

    public void connectionNode(Node node) {
        if (network.getLocalIps().contains(node.getIp())) {
            return;
        }
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "node connection", new Runnable() {
            @Override
            public void run() {
                node.setStatus(Node.WAIT);
                NettyClient client = new NettyClient(node);
                client.start();
            }
        }, true);
    }
}
