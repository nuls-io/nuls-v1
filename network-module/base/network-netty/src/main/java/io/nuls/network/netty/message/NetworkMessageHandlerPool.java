package io.nuls.network.netty.message;

import io.nuls.core.tools.log.Log;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.netty.broadcast.BroadcastHandler;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkMessageHandlerPool {

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    private BroadcastHandler broadcastHandler = BroadcastHandler.getInstance();
    private NetworkMessageHandlerFactory messageHandlerFactory = NetworkMessageHandlerFactory.getInstance();

    public void execute(BaseMessage message, Node node) {
        executorService.execute(new Thread() {
            @Override
            public void run() {
                BaseNetworkMeesageHandler handler = messageHandlerFactory.getHandler(message);
                try {
                    NetworkEventResult messageResult = handler.process(message, node);
                    if (messageResult != null && messageResult.getReplyMessage() != null) {
                        broadcastHandler.broadcastToNode((BaseMessage) messageResult.getReplyMessage(), node, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error(e);
                }
            }
        });
    }

    public static void shutdown() {
        executorService.shutdown();
    }
}
