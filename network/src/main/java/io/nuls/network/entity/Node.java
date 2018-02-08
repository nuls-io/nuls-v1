/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.network.entity;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsVersion;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.EventManager;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.mesasge.NulsMessage;
import io.nuls.core.mesasge.NulsMessageHeader;
import io.nuls.core.thread.manager.TaskManager;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.event.bus.service.intf.EventBusService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.*;
import io.nuls.network.message.entity.GetVersionEvent;
import io.nuls.network.message.entity.VersionEvent;
import io.nuls.network.message.filter.MessageFilterChain;
import io.nuls.network.message.handler.NetWorkEventHandler;
import io.nuls.network.module.AbstractNetworkModule;
import io.nuls.network.service.MessageWriter;
import io.nuls.network.service.NetworkService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class Node extends BaseNulsData {

    public static final short OWN_MAIN_VERSION = 1;

    public static final short OWN_SUB_VERSION = 1001;

    private int magicNumber;

    private String hash;

    private String ip;

    private Integer port;

    private Long lastTime;

    private Long lastFailTime;

    private Integer failCount;

    private Set<NodeGroup> groupSet;

    /**
     * 1: inNode ,  2: outNode
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

    private VersionEvent versionMessage;

    private Lock lock = new ReentrantLock();

    private EventBusService eventBusService;

    private NetworkEventHandlerFactory messageHandlerFactory;

    public Node() {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
    }

    public Node(AbstractNetworkParam network) {
        super(OWN_MAIN_VERSION, OWN_SUB_VERSION);
        this.magicNumber = network.packetMagic();
        this.messageHandlerFactory = network.getMessageHandlerFactory();
        eventBusService = NulsContext.getServiceBean(EventBusService.class);
        this.groupSet = new HashSet<>();
    }

    public Node(AbstractNetworkParam network, int type) {
        this(network);
        this.type = type;
    }


    public Node(AbstractNetworkParam network, int type, InetSocketAddress socketAddress) {
        this(network, type);
        this.port = socketAddress.getPort();
        this.ip = socketAddress.getAddress().getHostAddress();
        this.hash = this.ip + this.port;
    }

    public void connectionOpened() throws IOException {
        GetVersionEvent event = new GetVersionEvent(AbstractNetworkModule.ExternalPort);
        sendNetworkEvent(event);
        this.status = Node.HANDSHAKE;
    }

    public void sendMessage(NulsMessage message) throws IOException {
        if (this.getStatus() == Node.CLOSE) {
            return;
        }
        if (writeTarget == null || this.status != Node.HANDSHAKE) {
            throw new NotYetConnectedException();
        }
        lock.lock();
        try {
            this.writeTarget.write(message.serialize());
        } finally {
            lock.unlock();
        }
    }

    public void sendNetworkEvent(BaseEvent event) throws IOException {
        if (this.getStatus() == Node.CLOSE) {
            return;
        }
        if (writeTarget == null) {
            throw new NotYetConnectedException();
        }
        if (this.status != Node.HANDSHAKE && !isHandShakeMessage(event)) {
            throw new NotYetConnectedException();
        }
        lock.lock();
        try {
            byte[] data = event.serialize();
            NulsMessage message = new NulsMessage(magicNumber, data);
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
        if (this.getStatus() == Node.CLOSE) {
            return;
        }
        if (buffer.position() != 0 || buffer.limit() <= NulsMessageHeader.MESSAGE_HEADER_SIZE || buffer.limit() > NetworkConstant.MESSAGE_MAX_SIZE) {
            buffer.clear();
            throw new NulsVerificationException(ErrorCode.DATA_ERROR);
        }

        NulsMessage message = new NulsMessage(buffer);

//        buffer.compact();
        buffer.clear();
        try {
            if (MessageFilterChain.getInstance().doFilter(message)) {
                processMessage(message);
            }
        } catch (Exception e) {

        }

    }

    /**
     * if message is not a networkEvent, put it in eventProducer ,other module will consume it
     *
     * @param message
     */
    public void processMessage(NulsMessage message) throws IOException, NulsException {
        BaseEvent event = null;
        try {
            event = EventManager.getInstance(message.getData());
        } catch (Exception e) {
            //todo
            Log.error(e);
            event = null;
        }
        if (event == null) {
            return;
        }

        if (isNetworkEvent(event)) {
            if (this.status != Node.HANDSHAKE && !isHandShakeMessage(event)) {
                return;
            }

            BaseEvent networkEvent = (BaseEvent) event;
            asynExecute(networkEvent);
        } else {

            if (this.status != Node.HANDSHAKE) {
                return;
            }
//            if (checkBroadcastExist(message.getData())) {
//                return;
//            }
            //todo debug
            Log.info("==recieve:"+event.getClass());
            eventBusService.publishNetworkEvent(event, this.getHash());
        }
    }

    private void asynExecute(BaseEvent networkEvent) {
        NetWorkEventHandler handler = messageHandlerFactory.getHandler(networkEvent);
        TaskManager.asynExecuteRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    NetworkEventResult messageResult = handler.process(networkEvent, Node.this);
                    processMessageResult(messageResult);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.error("process message error", e);
                    Node.this.destroy();
                }
            }
        });
    }

//    public boolean checkBroadcastExist(byte[] data) throws IOException {
//        String hash = Sha256Hash.twiceOf(data).toString();
//        BroadcastResult result = NetworkCacheService.getInstance().getBroadCastResult(hash);
//        if (result == null) {
//            return false;
//        }
//
//        result.setRepliedCount(result.getRepliedCount() + 1);
//        if (result.getRepliedCount() < result.getWaitReplyCount()) {
//            NetworkCacheService.getInstance().addBroadCastResult(result);
//        } else {
//            ReplyNotice event = new ReplyNotice();
//            event.setEventBody(new BasicTypeData<>(data));
//            eventBusService.publishLocalEvent(event);
//        }
//        return true;
//    }


    public void processMessageResult(NetworkEventResult eventResult) throws IOException {
        if (this.getStatus() == Node.CLOSE) {
            return;
        }
        if (eventResult == null || !eventResult.isSuccess()) {
            return;
        }
        if (eventResult.getReplyMessage() != null) {
            sendNetworkEvent(eventResult.getReplyMessage());
        }
    }

    public void destroy() {
//        NulsContext.getServiceBean(NetworkService.class).removeNode(this.getHash());
//        lock.lock();
//        try {
//            this.lastFailTime = TimeService.currentTimeMillis();
//            this.status = Node.CLOSE;
//            if (this.writeTarget != null) {
//                this.writeTarget.closeConnection();
//                this.writeTarget = null;
//            }
//        } finally {
//            lock.unlock();
//        }
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
        eventBusService = NulsContext.getServiceBean(EventBusService.class);
    }


    public boolean isHandShakeMessage(BaseEvent event) {
        if (isNetworkEvent(event)) {
            if (event.getHeader().getEventType() == NetworkConstant.NETWORK_GET_VERSION_EVENT
                    || event.getHeader().getEventType() == NetworkConstant.NETWORK_VERSION_EVENT) {
                return true;
            }
        }
        return false;
    }

    public boolean isHandShake() {
        return this.status == Node.HANDSHAKE;
    }

    private boolean isNetworkEvent(BaseEvent event) {
        return event.getHeader().getModuleId() == NulsConstant.MODULE_ID_NETWORK;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{node:{");
        sb.append("ip:' " + getIp() + "',");
        sb.append("port: " + getPort() + ",");
        if (lastTime == null) {
            lastTime = System.currentTimeMillis();
        }

        sb.append("lastTime: " + DateUtil.convertDate(new Date(lastTime)) + ",");
        sb.append("magicNumber: " + magicNumber + "}}");
        return sb.toString();
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

    public VersionEvent getVersionMessage() {
        return versionMessage;
    }

    public void setVersionMessage(VersionEvent versionMessage) {
        this.versionMessage = versionMessage;
    }


    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Integer getFailCount() {
        if (failCount == null) {
            failCount = 0;
        }
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

    public void setMessageHandlerFactory(NetworkEventHandlerFactory factory) {
        this.messageHandlerFactory = factory;
    }

    public NetworkEventHandlerFactory getMessageHandlerFactory() {
        return this.messageHandlerFactory;
    }

    @Override
    public boolean equals(Object obj) {
        Node other = (Node) obj;
        if (StringUtils.isBlank(other.getHash())) {
            return false;
        }
        return other.getHash().equals(this.hash);
    }

    public void addToGroup(NodeGroup nodeGroup) {
        if (nodeGroup != null) {
            this.groupSet.add(nodeGroup);
        }
    }

    public void removeFromGroup(NodeGroup nodeGroup) {
        if (nodeGroup != null) {
            this.groupSet.remove(nodeGroup);
        }
    }

    public int getGroupCount(String groupName) {
        return this.groupSet.size();
    }

    public Set<NodeGroup> getGroupSet() {
        return this.groupSet;
    }

    public Long getLastFailTime() {
        if (lastFailTime == null) {
            lastFailTime = 0L;
        }
        return lastFailTime;
    }

    public void setLastFailTime(Long lastFailTime) {
        this.lastFailTime = lastFailTime;
    }
}
