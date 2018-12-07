/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.network.util;

import io.nuls.core.tools.log.Log;
import io.nuls.network.connection.netty.NettyClient;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NetworkMessageHandlerFactory;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.protocol.message.base.BaseMessage;

import java.util.concurrent.*;

public class NetworkThreadPool {


    private static final ExecutorService executor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());


    private static final ExecutorService executorConnector = new ThreadPoolExecutor(10, Integer.MAX_VALUE,
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
        executorConnector.submit(new Runnable() {
            @Override
            public void run() {
                NettyClient client = new NettyClient(node);
                client.start();
                System.out.println("---------------------------nettyClient end------------" + node.getIp());
            }
        });
    }
}
