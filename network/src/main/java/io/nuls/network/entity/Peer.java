package io.nuls.network.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.AbstractNetWorkMessageHandlerFactory;
import io.nuls.network.message.AbstractNetworkMessage;
import io.nuls.network.message.NetworkMessageResult;
import io.nuls.network.message.entity.GetVersionMessage;
import io.nuls.network.message.entity.VersionMessage;
import io.nuls.network.message.messageHandler.NetWorkMessageHandler;
import io.nuls.network.service.MessageWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class Peer extends BaseNulsData {

    private AbstractNetworkParam network;

    private String hash;

    private String ip;

    private int port;

    private long lastTime;

    private int failCount;

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

    private AbstractNetWorkMessageHandlerFactory messageHandlerFactory;

    /**
     * 异步顺序执行所有接收到的消息，以免有处理时间较长的线程阻塞，影响性能
     */
    //todo 要使用带有ThreadFactory参数的ThreadPoolExecutor构造方法哦，这样你就可以方便的设置线程名字啦。
    private ExecutorService executorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    public Peer(AbstractNetworkParam network) {
        this.network = network;
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);
    }

    public Peer(AbstractNetworkParam network, int type) {
        this.network = network;
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        this.type = type;
        processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);
    }


    public Peer(AbstractNetworkParam network, int type, InetSocketAddress socketAddress) {
        this.network = network;
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        this.type = type;
        this.port = socketAddress.getPort();
        this.ip = socketAddress.getAddress().getHostAddress();
        processorService = NulsContext.getInstance().getService(NetworkProcessorService.class);


        this.hash = this.ip + this.port;
    }

    public void connectionOpened() throws IOException {
        GetVersionMessage message = new GetVersionMessage();
        sendMessage(message);
        this.status = Peer.CONNECTING;
    }


    public void sendMessage(NulsMessage message) throws IOException {
        if (this.getStatus() == Peer.CLOSE) {
            return;
        }
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

    public void sendMessage(AbstractNetworkMessage networkMessage) throws IOException {
        if (this.getStatus() == Peer.CLOSE) {
            return;
        }
        if (writeTarget == null) {
            throw new NotYetConnectedException();
        }
        if (this.status != Peer.HANDSHAKE && !isHandShakeMessage(networkMessage)) {
            throw new NotYetConnectedException();
        }

        byte[] data = networkMessage.serialize();
        NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.NETWORK_MESSAGE, data);
        sendMessage(message);
    }

    /**
     * process the receive message
     *
     * @throws IOException
     */
    public void receiveMessage(ByteBuffer buffer) throws IOException {
        if (this.getStatus() == Peer.CLOSE) {
            return;
        }
        if (buffer.position() != 0 || buffer.capacity() <= NulsMessageHeader.MESSAGE_HEADER_SIZE) {
            throw new NulsVerificationException(ErrorCode.NET_MESSAGE_ERROR);
        }

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
            AbstractNetworkMessage networkMessage = AbstractNetworkMessage.transfer(msgType, data);
            if (this.status != Peer.HANDSHAKE && !isHandShakeMessage(networkMessage)) {
                return;
            }

            NetWorkMessageHandler handler = messageHandlerFactory.getHandler(networkMessage);
            executorService.submit(new Thread() {
                //todo 不要显示创建线程，请使用线程池。
                @Override
                public void run() {
                    try {
                        NetworkMessageResult messageResult = handler.process(networkMessage, Peer.this);
                        processMessageResult(messageResult);
                    } catch (Exception e) {
                        Peer.this.destroy();
                    }
                }
            });
        }
    }


    public void processMessageResult(NetworkMessageResult messageResult) throws IOException {
        if (this.getStatus() == Peer.CLOSE) {
            return;
        }
        if (messageResult == null) {
            return;
        }
        if (messageResult.getReplyMessage() != null) {
            sendMessage(messageResult.getReplyMessage());
        }
    }

    public void destroy() {
        System.out.println("---------peer destory:" + this.getIp());
        lock.lock();
        try {
            this.status = Peer.CLOSE;
            if (this.writeTarget != null) {
                this.writeTarget.closeConnection();
                this.writeTarget = null;
            }
        } finally {
            lock.unlock();
        }
        //todo check failCount and save or remove from database
        //this.failCount ++;

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


    public boolean isHandShakeMessage(AbstractNetworkMessage networkMessage) {
//        return networkMessage instanceof VersionMessage;
        return (networkMessage.getType() == NetworkConstant.NETWORK_GET_VERSION_MESSAGE
                || networkMessage.getType() == NetworkConstant.NETWORK_VERSION_MESSAGE);

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

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }
}
