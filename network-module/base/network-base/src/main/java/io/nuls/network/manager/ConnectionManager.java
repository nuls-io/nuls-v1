package io.nuls.network.manager;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Component;
import io.nuls.kernel.thread.manager.TaskManager;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.connection.netty.NettyClient;
import io.nuls.network.connection.netty.NettyServer;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.NetworkEventResult;
import io.nuls.network.entity.Node;
import io.nuls.network.message.filter.MessageFilterChain;
import io.nuls.network.protocol.handler.BaseNetworkMeesageHandler;
import io.nuls.network.protocol.message.BaseNetworkMessage;
import io.nuls.network.service.NetworkService;
import io.nuls.protocol.message.base.BaseMessage;
import io.nuls.protocol.message.base.MessageHeader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Component
public class ConnectionManager {

    private NetworkParam network = NetworkParam.getInstance();

    private NettyServer nettyServer;

    @Autowired
    private NetworkService networkService;

    private NetworkMessageHandlerFactory messageHandlerFactory = NetworkMessageHandlerFactory.getInstance();

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

        TaskManager.createAndRunThread(NetworkConstant.NETWORK_MODULE_ID, "node connection", new Runnable() {
            @Override
            public void run() {
                node.setStatus(Node.WAIT);
                NettyClient client = new NettyClient(node);
                client.start();
            }
        }, true);
    }

    public void receiveMessage(ByteBuffer buffer, Node node) {
        List<BaseMessage> list;
        try {
            buffer.flip();
            if (!node.isAlive()) {
                buffer.clear();
                return;
            }
            list = new ArrayList<>();
            byte[] bytes = buffer.array();
            int offset = 0;
            while (offset < bytes.length - 1) {
                BaseMessage message = new BaseMessage();
                message.parse(bytes);
                list.add(message);
                offset = message.serialize().length;
                if (bytes.length > offset) {
                    byte[] subBytes = new byte[bytes.length - offset];
                    System.arraycopy(bytes, offset, subBytes, 0, subBytes.length);
                    bytes = subBytes;
                    offset = 0;
                }
            }
            for (BaseMessage message : list) {
                if (MessageFilterChain.getInstance().doFilter(message)) {
                    MessageHeader header = message.getHeader();

                    if (node.getMagicNumber() == 0) {
                        node.setMagicNumber(header.getMagicNumber());
                    }

                    processMessage(message, node);
                } else {
                    node.setStatus(Node.BAD);
//                    System.out.println("-------------------- receive message filter remove node ---------------------------");
                    networkService.removeNode(node.getId());
                }
            }
        } catch (Exception e) {
            Log.error("remoteAddress: " + node.getId());
            Log.error(e);
            return;
        } finally {
            buffer.clear();
        }
    }


    private void processMessage(BaseMessage message, Node node) {
        if (message == null) {
//            Log.error("---------------------message is null--------------------------------");
            return;
        }
        if (isNetworkMessage(message)) {
            if (node.getStatus() != Node.HANDSHAKE && !isHandShakeMessage(message)) {
                return;
            }
            // System.out.println( sdf.format(System.currentTimeMillis()) + "-----------processMessage------------node:" + node.getId() + "------------moduleId: " + event.getHeader().getModuleId() + "," + "eventType:" + event.getHeader().getEventType());
            asynExecute(message, node);
        } else {
            if (!node.isHandShake()) {
                return;
            }
            //todo 传给其他模块处理
            //eventBusService.publishNetworkEvent(event, node.getId());
        }
    }

    private void asynExecute(BaseMessage message, Node node) {
        BaseNetworkMeesageHandler handler = messageHandlerFactory.getHandler(message);
        TaskManager.asynExecuteRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    NetworkEventResult messageResult = handler.process((BaseNetworkMessage) message, node);
                    processMessageResult(messageResult, node);
                } catch (Exception e) {
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

    public void processMessageResult(NetworkEventResult eventResult, Node node) throws IOException {
        if (node.getStatus() == Node.CLOSE) {
            return;
        }
        if (eventResult == null || !eventResult.isSuccess()) {
            return;
        }
        if (eventResult.getReplyMessage() != null) {
            networkService.sendToNode(eventResult.getReplyMessage(), node, true);
        }
    }

    private boolean isNetworkMessage(BaseMessage message) {
        return message.getHeader().getModuleId() == NetworkConstant.NETWORK_MODULE_ID;
    }

    private boolean isHandShakeMessage(BaseMessage message) {
        if (message.getHeader().getMsgType() == NetworkConstant.NETWORK_HANDSHAKE) {
            return true;
        }
        return false;
    }
}
