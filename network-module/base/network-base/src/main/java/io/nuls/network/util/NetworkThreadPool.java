package io.nuls.network.util;

import io.nuls.core.tools.log.Log;
import io.nuls.network.connection.netty.NettyClient;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NetworkMessageHandlerFactory;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.concurrent.*;

public class NetworkThreadPool {

    private static final ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    public static void asynNetworkMessage(BaseMessage message, Node node, HeartBeatThread heartBeatThread, NetworkMessageHandlerFactory messageHandlerFactory, ConnectionManager connectionManager) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                if (message.getHeader().getMsgType() == NetworkConstant.NETWORK_VERSION) {
                    heartBeatThread.offerMessage(message, node);
                    return;
                }
                BaseNetworkMeesageHandler handler = messageHandlerFactory.getHandler(message);
                try {
                    NetworkEventResult messageResult = handler.process(message, node);
                    connectionManager.processMessageResult(messageResult, node);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error(e);
                }
            }
        });
    }

    public static void doConnect(Node node) {

        executor.submit(new Runnable() {
            @Override
            public void run() {
                NettyClient client = new NettyClient(node);
                client.start();
            }
        });
    }
}
