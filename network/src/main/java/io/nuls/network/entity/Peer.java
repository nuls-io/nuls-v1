package io.nuls.network.entity;

import io.nuls.core.chain.entity.Block;
import io.nuls.core.chain.entity.NulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.network.entity.param.NetworkParam;
import io.nuls.network.message.NetWorkMessageHandlerFactory;
import io.nuls.network.message.NetworkMessage;
import io.nuls.network.message.NetworkMessageResult;
import io.nuls.network.message.entity.VersionMessage;
import io.nuls.network.message.messageHandler.NetWorkMessageHandler;
import io.nuls.network.service.MessageWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Peer extends NulsData {

    private NetworkParam network;

    private String hash;

    private String ip;

    private int port;

    /**
     * 1: inPeer ,  2: outPeer
     */
    public final static int IN = 1;
    public final static int OUT = 2;
    private int type;

    /**
     * 0: wait , 1: connecting, 2: handshake 3: close
     */
    public final static int WAIT = 0;
    public final static int CONNECTING = 1;
    public final static int HANDSHAKE = 2;
    public final static int CLOSE = 3;
    private volatile int status;

    private MessageWriter writeTarget;

    private VersionMessage versionMessage;

    private Lock lock = new ReentrantLock();

    private NetworkProcessorService processorService;

    private NetWorkMessageHandlerFactory messageHandlerFactory;

    //异步顺序执行所有接收到的消息，以免有处理时间较长的线程阻塞，影响性能
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Peer(NetworkParam network) {
        this.network = network;
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);
    }

    public Peer(NetworkParam network, int type) {
        this.network = network;
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        this.type = type;
        processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);
    }


    public Peer(NetworkParam network, int type, InetSocketAddress socketAddress) {
        this.network = network;
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        this.type = type;
        this.port = socketAddress.getPort();
        this.ip = socketAddress.getAddress().getHostAddress();
        processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);
    }

    public void connect(NetworkParam network) {
//

    }

    public void connectionOpened() throws IOException {
        Block bestBlock = NulsContext.getInstance().getBestBlock();
        VersionMessage message = new VersionMessage(bestBlock.getHeight(), bestBlock.getHash().toString(), this);
        sendMessage(message);
    }


    public void sendMessage(NulsMessage message) throws IOException {
        if (writeTarget == null || this.status != Peer.HANDSHAKE) {
            throw new NotYetConnectedException();
        }

        lock.lock();
        try {
            this.writeTarget.write(message.serialize());
        } finally {
            lock.unlock();
        }
    }

    public void sendMessage(NetworkMessage networkMessage) throws IOException {
        if (writeTarget == null) {
            throw new NotYetConnectedException();
        }
        if (this.status != Peer.HANDSHAKE && !isHandShakeMessage(networkMessage)) {
            throw new NotYetConnectedException();
        }

        byte[] data = networkMessage.serialize();
        byte b = 1;
        NulsMessage message = new NulsMessage(network.packetMagic(), data.length, NulsMessageHeader.NETWORK_MESSAGE, b, data);
        sendMessage(message);
    }

    /**
     * process the receive message
     *
     * @throws IOException
     */
    public void receiveMessage(ByteBuffer buffer) throws IOException {
        if (buffer.position() != 0 || buffer.capacity() <= NulsMessageHeader.MESSAGE_HEADER_SIZE) {
            throw new NulsRuntimeException(ErrorCode.DATA_ERROR);
        }

        //todo there can use NulsMessageFilter
        byte[] headers = new byte[NulsMessageHeader.MESSAGE_HEADER_SIZE];
        buffer.get(headers, 0, headers.length);
        NulsMessageHeader messageHeader = network.getMessageFilter().filterHeader(headers);
        byte[] data = new byte[buffer.limit() - headers.length];
        buffer.get(data, 0, data.length);

        processMessage(messageHeader, data);
    }

    /**
     * if message is a eventMessage , put it in processorService ,other module will process it
     *
     * @param messageHeader
     * @param data
     */
    public void processMessage(NulsMessageHeader messageHeader, byte[] data) {
        if (messageHeader.getHeadType() == NulsMessageHeader.EVENT_MESSAGE) {
            if (this.status != Peer.HANDSHAKE) {
                return;
            }

            NulsMessage message = new NulsMessage(messageHeader, data);
            processorService.send(message.getData());
        } else {

            byte[] types = new byte[2];
            System.arraycopy(data, 0, types, 0, 2);
            short msgType = Utils.bytes2Short(types);
            NetworkMessage networkMessage = NetworkMessage.transfer(msgType, data);
            if (this.status != Peer.HANDSHAKE && !isHandShakeMessage(networkMessage)) {
                return;
            }

            NetWorkMessageHandler handler = messageHandlerFactory.getHandler(networkMessage);
            executorService.submit(new Thread() {
                @Override
                public void run() {
                    NetworkMessageResult messageResult = handler.process(networkMessage, Peer.this);
                    processMessageResult(messageResult);
                }
            });
        }
    }


    public void processMessageResult(NetworkMessageResult messageResult) {

    }

    public void destroy() {
        lock.lock();
        try {
            this.status = 2;
            this.writeTarget.closeConnection();
            this.writeTarget = null;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void serializeToStream(OutputStream stream) throws IOException {

    }

    @Override
    public void parse(NulsByteBuffer buffer) {

    }


    public boolean isHandShakeMessage(NetworkMessage networkMessage) {
        return networkMessage instanceof VersionMessage;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public MessageWriter getWriteTarget() {
        return writeTarget;
    }

    public void setWriteTarget(MessageWriter writeTarget) {
        this.writeTarget = writeTarget;
    }

    public VersionMessage getVersionMessage() {
        return versionMessage;
    }

    public void setVersionMessage(VersionMessage versionMessage) {
        this.versionMessage = versionMessage;
    }
}
