/**
 * MIT License
 **
 * Copyright (c) 2017-2018 nuls.io
 **
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 **
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 **
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.service.impl;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.Node;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.NetworkEventHandlerFactory;
import io.nuls.network.message.NetworkEventResult;
import io.nuls.network.message.filter.MessageFilterChain;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.service.NetworkService;
import io.nuls.network.service.impl.netty.NettyClient;
import io.nuls.network.service.impl.netty.NettyServer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017-11-10
 */
public class ConnectionManager {

    private AbstractNetworkParam network;
    private NetworkService networkService;
    private NettyServer nettyServer;
    private EventBusService eventBusService;
    private NetworkEventHandlerFactory messageHandlerFactory;

    private static ConnectionManager instance = new ConnectionManager();

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    public void init() {
        nettyServer = new NettyServer(network.port());
        nettyServer.init();
        eventBusService = NulsContext.getServiceBean(EventBusService.class);
        messageHandlerFactory = network.getMessageHandlerFactory();
    }

    public void start() throws InterruptedException {
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_NETWORK, "node connection", new Runnable() {
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
        TaskManager.createAndRunThread(NulsConstant.MODULE_ID_NETWORK, "node connection", new Runnable() {
            @Override
            public void run() {
                node.setStatus(Node.WAIT);
                NettyClient client = new NettyClient(node);
                client.start();
            }
        }, true);
    }


    public void receiveMessage(ByteBuffer buffer, Node node) {
        List<NulsMessage> list;
        try {
            buffer.flip();
            if (!node.isAlive()) {
                buffer.clear();
                return;
            }
            list = new ArrayList<>();
            while (buffer.hasRemaining()) {
                NulsMessage message = new NulsMessage(buffer);
                list.add(message);
            }
            for (NulsMessage message : list) {
                if (MessageFilterChain.getInstance().doFilter(message)) {
                    NulsMessageHeader header = message.getHeader();
                    if (node.getMagicNumber() == 0) {
                        node.setMagicNumber(header.getMagicNumber());
                    }

                    BaseEvent event = EventManager.getInstance(message.getData());
                    processMessage(event, node);
                } else {
                    node.setStatus(Node.BAD);
                    System.out.println("-------------------- receive message filter remove node ---------------------------");
                    networkService.removeNode(node.getId());
                }
            }
        } catch (NulsException e) {
            //todo
            Log.error("--------remoteAddress: " + node.getId());
            Log.error(e);
        } catch (Exception e) {
            //todo
            Log.error(e);
            return;
        } finally {
            buffer.clear();
        }
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private void processMessage(BaseEvent event, Node node) {
        if (event == null) {
            Log.error("---------------------NulEvent is null--------------------------------");
            return;
        }
        if (isNetworkEvent(event)) {
            if (node.getStatus() != Node.HANDSHAKE && !isHandShakeMessage(event)) {
                return;
            }
           // System.out.println( sdf.format(System.currentTimeMillis()) + "-----------processMessage------------node:" + node.getId() + "------------moduleId: " + event.getHeader().getModuleId() + "," + "eventType:" + event.getHeader().getEventType());
            asynExecute(event, node);
        } else {
            if (!node.isHandShake()) {
                return;
            }
            eventBusService.publishNetworkEvent(event, node.getId());
        }
    }

    private void asynExecute(BaseEvent networkEvent, Node node) {
        NetWorkEventHandler handler = messageHandlerFactory.getHandler(networkEvent);
        TaskManager.asynExecuteRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    NetworkEventResult messageResult = handler.process(networkEvent, node);
                    processMessageResult(messageResult, node);
                } catch (Exception e) {
                    Log.error(e);
                }
            }
        });
    }

    public void processMessageResult(NetworkEventResult eventResult, Node node) throws IOException {
        if (node.getStatus() == Node.CLOSE) {
            return;
        }
        if (eventResult == null || !eventResult.isSuccess()) {
            return;
        }
        if (eventResult.getReplyMessage() != null) {
            networkService.sendToNode(eventResult.getReplyMessage(), node.getId(), true);
        }
    }

    private boolean isNetworkEvent(BaseEvent event) {
        return event.getHeader().getModuleId() == NulsConstant.MODULE_ID_NETWORK;
    }

    private boolean isHandShakeMessage(BaseEvent event) {
        if (isNetworkEvent(event) && event.getHeader().getEventType() == NetworkConstant.NETWORK_HANDSHAKE_EVENT) {
            return true;
        }
        return false;
    }

    public void setNetwork(AbstractNetworkParam network) {
        this.network = network;
    }

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

}
