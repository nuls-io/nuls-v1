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

package io.nuls.network.manager;

import io.netty.buffer.ByteBuf;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.message.bus.service.MessageBusService;
import io.nuls.network.connection.netty.NettyClient;
import io.nuls.network.connection.netty.NettyServer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.message.filter.MessageFilterChain;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.util.NetworkThreadPool;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.MessageHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {

    private static ConnectionManager instance = new ConnectionManager();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    private NetworkParam networkParam = NetworkParam.getInstance();

    private NettyServer nettyServer;

    private NodeManager nodeManager;

    private BroadcastHandler broadcastHandler;

    private NetworkMessageHandlerFactory messageHandlerFactory = NetworkMessageHandlerFactory.getInstance();

    private MessageBusService messageBusService = NulsContext.getServiceBean(MessageBusService.class);

    public void init() {
        nodeManager = NodeManager.getInstance();
        broadcastHandler = BroadcastHandler.getInstance();
        nettyServer = new NettyServer(networkParam.getPort());
        nettyServer.init();
//        eventBusService = NulsContext.getServiceBean(EventBusService.class);
//        messageHandlerFactory = network.getMessageHandlerFactory();
    }

    public void start() {
        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "node server start", new Runnable() {
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
        node.setStatus(Node.CONNECT);
        NetworkThreadPool.doConnect(node);
//        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "node connection", new Runnable() {
//            @Override
//            public void run() {
//                NettyClient client = new NettyClient(node);
//                client.start();
//            }
//        }, true);
    }

    public void receiveMessage(ByteBuf buffer, Node node) throws NulsException {
        List<BaseMessage> list;
        try {
            list = new ArrayList<>();
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            NulsByteBuffer byteBuffer = new NulsByteBuffer(bytes);
            while (!byteBuffer.isFinished()) {
                MessageHeader header = byteBuffer.readNulsData(new MessageHeader());
                byteBuffer.setCursor(byteBuffer.getCursor() - header.size());
                BaseMessage message = getMessageBusService().getMessageInstance(header.getModuleId(), header.getMsgType()).getData();
                message = byteBuffer.readNulsData(message);
                list.add(message);
            }
            for (BaseMessage message : list) {
                if (MessageFilterChain.getInstance().doFilter(message)) {
                    MessageHeader header = message.getHeader();

                    if (node.getMagicNumber() == 0L) {
                        node.setMagicNumber(header.getMagicNumber());
                    }

                    processMessage(message, node);
                } else {
                    node.setStatus(Node.BAD);
                    Log.debug("-------------------- receive message filter remove node ---------------------------" + node.getId());
                    nodeManager.removeNode(node.getId());
                }
            }
        } catch (Exception e) {
            throw new NulsException(KernelErrorCode.DATA_ERROR, e);
        } finally {
            buffer.clear();
        }
    }


    private void processMessage(BaseMessage message, Node node) {
        if (message == null) {
            return;
        }
        if (isNetworkMessage(message)) {
            if (node.getStatus() != Node.HANDSHAKE && !isHandShakeMessage(message)) {
                return;
            }
            asynExecute(message, node);
        } else {
            if (!node.isHandShake()) {
                return;
            }
            messageBusService.receiveMessage(message, node);
        }
    }

    private void asynExecute(BaseMessage message, Node node) {
        BaseNetworkMeesageHandler handler = messageHandlerFactory.getHandler(message);
        TaskManager.asynExecuteRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    NetworkEventResult messageResult = handler.process(message, node);
                    processMessageResult(messageResult, node);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error(e);
                }
            }

            @Override
            public String toString() {
                StringBuilder log = new StringBuilder();
                log.append("event: " + message.toString())
                        .append(", hash: " + message.getHash())
                        .append(", Node: " + node.toString());
                return log.toString();
            }
        });
    }

    public void processMessageResult(NetworkEventResult messageResult, Node node) throws IOException {
        if (!node.isAlive()) {
            return;
        }
        if (messageResult == null || !messageResult.isSuccess()) {
            return;
        }
        if (messageResult.getReplyMessage() != null) {
            broadcastHandler.broadcastToNode((BaseMessage) messageResult.getReplyMessage(), node, true);
        }
    }

    private boolean isNetworkMessage(BaseMessage message) {
        return message.getHeader().getModuleId() == NetworkConstant.NETWORK_MODULE_ID;
    }

    private boolean isHandShakeMessage(BaseMessage message) {
        if (message.getHeader().getMsgType() == NetworkConstant.NETWORK_HANDSHAKE ||
                message.getHeader().getMsgType() == NetworkConstant.NETWORK_P2P_NODE) {
            return true;
        }
        return false;
    }

    public MessageBusService getMessageBusService() {
        if (messageBusService == null) {
            messageBusService = NulsContext.getServiceBean(MessageBusService.class);
        }
        return messageBusService;
    }

    public void shutdown() {
        nettyServer.shutdown();
    }
}
