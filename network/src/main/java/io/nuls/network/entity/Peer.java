package io.nuls.network.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.event.bus.processor.service.intf.NetworkProcessorService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.AbstractNetWorkDataHandlerFactory;
import io.nuls.network.message.BaseNetworkData;
import io.nuls.network.message.NetworkDataHeader;
import io.nuls.network.message.NetworkDataResult;
import io.nuls.network.message.entity.GetVersionData;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.messageHandler.NetWorkDataHandler;
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

    private VersionData versionMessage;

    private Lock lock = new ReentrantLock();

    private NetworkProcessorService processorService;

    private AbstractNetWorkDataHandlerFactory messageHandlerFactory;


    public Peer(AbstractNetworkParam network) {
        this.network = network;
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        processorService = getProcessorService();
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
        GetVersionData data = new GetVersionData();
        sendNetworkData(data);
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

            System.out.println("---send message:" + Hex.encode(message.serialize()));
            this.writeTarget.write(message.serialize());
        } finally {
            lock.unlock();
        }
    }

    public void sendNetworkData(BaseNetworkData networkData) throws IOException {
        if (this.getStatus() == Peer.CLOSE) {
            return;
        }
        if (writeTarget == null) {
            throw new NotYetConnectedException();
        }
        if (this.status != Peer.HANDSHAKE && !isHandShakeMessage(networkData)) {
            throw new NotYetConnectedException();
        }
        lock.lock();
        try {
            byte[] data = networkData.serialize();
            NulsMessage message = new NulsMessage(network.packetMagic(), NulsMessageHeader.NETWORK_MESSAGE, data);
            this.writeTarget.write(message.serialize());
        } finally {
            lock.unlock();
        }
    }

    /**
     * process the receive message
     *
     * @throws IOException
     */
    public void receiveMessage(ByteBuffer buffer) throws IOException {
        buffer.flip();
        if (this.getStatus() == Peer.CLOSE) {
            return;
        }
        if (buffer.position() != 0 || buffer.limit() <= NulsMessageHeader.MESSAGE_HEADER_SIZE || buffer.limit() > NulsMessage.MAX_SIZE) {
            buffer.clear();
            throw new NulsVerificationException(ErrorCode.DATA_ERROR);
        }

        NulsMessage message = new NulsMessage(buffer);

//        buffer.compact();
        buffer.clear();
        processMessage(message);

    }

    /**
     * if message is a eventMessage , put it in processorService ,other module will process it
     *
     * @param message
     */
    public void processMessage(NulsMessage message) {


        if (message.getHeader().getHeadType() == NulsMessageHeader.EVENT_MESSAGE) {
            try {
                System.out.println("---receive message:" + Hex.encode(message.serialize()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (this.status != Peer.HANDSHAKE) {
                return;
            }
            message.verify();
           // processorService.send(message.getData());
        } else {
            byte[] networkHeader = new byte[NetworkDataHeader.NETWORK_HEADER_SIZE];
            System.arraycopy(message.getData(), 0, networkHeader, 0, NetworkDataHeader.NETWORK_HEADER_SIZE);
            NetworkDataHeader header = new NetworkDataHeader(new NulsByteBuffer(networkHeader));

            BaseNetworkData networkMessage = BaseNetworkData.transfer(header.getType(), message.getData());
            if (this.status != Peer.HANDSHAKE && !isHandShakeMessage(networkMessage)) {
                return;
            }

            NetWorkDataHandler handler = messageHandlerFactory.getHandler(networkMessage);
            ThreadManager.asynExecuteRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        NetworkDataResult messageResult = handler.process(networkMessage, Peer.this);
                        processMessageResult(messageResult);
                    } catch (Exception e) {
                        Log.error("process message error", e);
                        //e.printStackTrace();
                        Peer.this.destroy();
                    }
                }
            });
        }
    }


    public void processMessageResult(NetworkDataResult dataResult) throws IOException {
        if (this.getStatus() == Peer.CLOSE) {
            return;
        }
        if (dataResult == null) {
            return;
        }
        if (dataResult.getReplyMessage() != null) {
            sendNetworkData(dataResult.getReplyMessage());
        }
    }

    public void destroy() {

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


    public boolean isHandShakeMessage(BaseNetworkData networkMessage) {
//        return networkMessage instanceof VersionData;
        return (networkMessage.getNetworkHeader().getType() == NetworkConstant.NETWORK_GET_VERSION_MESSAGE
                || networkMessage.getNetworkHeader().getType() == NetworkConstant.NETWORK_VERSION_MESSAGE);

    }

    @Override
    public boolean equals(Object o) {
        Peer other = (Peer) o;
        return this.getHash().equals(other.getHash());
    }


    private NetworkProcessorService getProcessorService() {

        while (NulsContext.getInstance().getService(NetworkProcessorService.class) == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return NulsContext.getInstance().getService(NetworkProcessorService.class);
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

    public VersionData getVersionMessage() {
        return versionMessage;
    }

    public void setVersionMessage(VersionData versionMessage) {
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
