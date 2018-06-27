package io.nuls.network.util;

import io.netty.buffer.ByteBuf;
import io.nuls.core.tools.log.Log;
import io.nuls.network.connection.netty.NettyClient;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeManager;
import io.nuls.network.model.Node;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NetworkThreadPool {

    private static final ExecutorService executor = new ThreadPoolExecutor(10, 20, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(2000));//CPU核数4-10倍

    public static void doRead(ByteBuf buffer, Node node) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    ConnectionManager.getInstance().receiveMessage(buffer, node);
                } catch (Exception e) {
                    Log.error(e);
                    NodeManager.getInstance().removeNode(node);
                } finally {
                    buffer.release();
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
