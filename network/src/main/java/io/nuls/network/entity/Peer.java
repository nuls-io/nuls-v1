package io.nuls.network.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.thread.manager.ThreadManager;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.processor.service.intf.NoticeProcessorService;
import io.nuls.event.bus.processor.service.intf.EventProcessorService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.*;
import io.nuls.network.message.entity.GetVersionData;
import io.nuls.network.message.entity.VersionData;
import io.nuls.network.message.messageHandler.NetWorkDataHandler;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.service.MessageWriter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class Peer extends BaseNulsData {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private int magicNumber;

    private String hash;

    private String ip;

    private Integer port;

    private Long lastTime;

    private Integer failCount;

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

    private EventProcessorService eventProcessorService;

    private NoticeProcessorService noticeProcessorService;

    private AbstractNetWorkDataHandlerFactory messageHandlerFactory;

    public Peer() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public Peer(AbstractNetworkParam network) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.magicNumber = network.packetMagic();
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        eventProcessorService = NulsContext.getInstance().getService(EventProcessorService.class);
        noticeProcessorService = NulsContext.getInstance().getService(NoticeProcessorService.class);
    }

    public Peer(AbstractNetworkParam network, int type) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.magicNumber = network.packetMagic();
        this.type = type;
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        eventProcessorService = NulsContext.getInstance().getService(EventProcessorService.class);
        noticeProcessorService = NulsContext.getInstance().getService(NoticeProcessorService.class);
    }


    public Peer(AbstractNetworkParam network, int type, InetSocketAddress socketAddress) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.magicNumber = network.packetMagic();
        this.type = type;
        this.port = socketAddress.getPort();
        this.ip = socketAddress.getAddress().getHostAddress();
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        eventProcessorService = NulsContext.getInstance().getService(EventProcessorService.class);
        noticeProcessorService = NulsContext.getInstance().getService(NoticeProcessorService.class);
        this.hash = this.ip + this.port;
    }

    public void connectionOpened() throws IOException {
        GetVersionData data = new GetVersionData(AbstractNetworkModule.ExternalPort);
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
            NulsMessage message = new NulsMessage(magicNumber, NulsMessageHeader.NETWORK_MESSAGE, data);
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
    public void receiveMessage(ByteBuffer buffer) throws IOException, NulsException {
        buffer.flip();
        if (this.getStatus() == Peer.CLOSE) {
            return;
        }
        if (buffer.position() != 0 || buffer.limit() <= NulsMessageHeader.MESSAGE_HEADER_SIZE || buffer.limit() > NetworkConstant.MESSAGE_MAX_SIZE) {
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
    public void processMessage(NulsMessage message) throws IOException {
        if (message.getHeader().getHeadType() == NulsMessageHeader.EVENT_MESSAGE) {
            try {
                System.out.println("------receive message:" + Hex.encode(message.serialize()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (this.status != Peer.HANDSHAKE) {
                return;
            }
            if (checkBroadcastExist(message.getData())) {
                return;
            }
            eventProcessorService.dispatch(message.getData(), this.getHash());
        } else {

            byte[] networkHeader = new byte[NetworkDataHeader.NETWORK_HEADER_SIZE];
            System.arraycopy(message.getData(), 0, networkHeader, 0, NetworkDataHeader.NETWORK_HEADER_SIZE);
            NetworkDataHeader header;
            BaseNetworkData networkMessage;
            try {
                header = new NetworkDataHeader(new NulsByteBuffer(networkHeader));
                networkMessage = BaseNetworkData.transfer(header.getType(), message.getData());
            } catch (NulsException e) {
                Log.error("networkMessage transfer error:", e);
                this.destroy();
                return;
            }

            if (this.status != Peer.HANDSHAKE && !isHandShakeMessage(networkMessage)) {
                return;
            }
            asynExecute(networkMessage);
        }
    }

    private void asynExecute(BaseNetworkData networkMessage) {
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

    public boolean checkBroadcastExist(byte[] data) throws IOException {
        String hash = Sha256Hash.twiceOf(data).toString();
        BroadcastResult result = NetworkCacheService.getInstance().getBroadCastResult(hash);
        if (result == null) {
            return false;
        }

        result.setRepliedCount(result.getRepliedCount() + 1);
        if (result.getRepliedCount() < result.getWaitReplyCount()) {
            NetworkCacheService.getInstance().addBroadCastResult(result);
        } else {
            ReplyNotice event = new ReplyNotice();
            event.setEventBody(new BasicTypeData<>(data));
            noticeProcessorService.notice(event);
        }
        return true;
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
    }

    @Override
    public int size() {
        int s = 0;
        s += 2;   //version size;
        s += VarInt.sizeOf(magicNumber);
        s += VarInt.sizeOf(port);
        s += 1;
        try {
            s += ip.getBytes(NulsContext.DEFAULT_ENCODING).length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(version.getVersion());
        stream.write(new VarInt(magicNumber).encode());
        stream.write(new VarInt(port).encode());
        stream.writeString(ip);
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        version = new NulsVersion(buffer.readShort());
        magicNumber = (int) buffer.readVarInt();
        port = (int) buffer.readVarInt();
        ip = new String(buffer.readByLengthByte());
        eventProcessorService = NulsContext.getInstance().getService(EventProcessorService.class);
        noticeProcessorService = NulsContext.getInstance().getService(NoticeProcessorService.class);
    }


    public boolean isHandShakeMessage(BaseNetworkData networkMessage) {
        return (networkMessage.getNetworkHeader().getType() == NetworkConstant.NETWORK_GET_VERSION_MESSAGE
                || networkMessage.getNetworkHeader().getType() == NetworkConstant.NETWORK_VERSION_MESSAGE);
    }

    public boolean isHandShake() {
        return this.status == Peer.HANDSHAKE;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("peer:{");
        buffer.append("ip: " + getIp() + ",");
        buffer.append("port: " + getPort() + ",");
        if (lastTime == null) {
            lastTime = System.currentTimeMillis();
        }

        buffer.append("lastTime: " + DateUtil.convertDate(new Date(lastTime)) + ",");
        buffer.append("magicNumber: " + magicNumber + "}");
        return buffer.toString();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHash() {
        if (StringUtils.isBlank(hash)) {
            hash = ip + port;
        }
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


    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public void setMessageHandlerFactory(AbstractNetWorkDataHandlerFactory factory) {
        this.messageHandlerFactory = factory;
    }

    public AbstractNetWorkDataHandlerFactory getMessageHandlerFactory() {
        return this.messageHandlerFactory;
    }

    @Override
    public boolean equals(Object obj) {
        Peer other = (Peer) obj;
        if (StringUtils.isBlank(other.getHash())) {
            return false;
        }
        return other.getHash().equals(this.hash);
    }
}
