/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.network.model;

import io.netty.channel.Channel;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.network.listener.EventListener;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vivi
 */
public class Node extends BaseNulsData {

    private String id;

    private String ip;

    private Integer port;

    private long magicNumber;

    private Long lastTime;

    private int failCount;

    private Long lastFailTime;

    private long bestBlockHeight;

    private NulsDigestData bestBlockHash;

    private String externalIp;

    private boolean isSeedNode;

    private Long timeOffset;

    /**
     * 1: inNode ,  2: outNode
     */
    public final static int IN = 1;
    public final static int OUT = 2;
    private int type;

    /**
     * 0: uncheck , 1: connectable, 2: unavailable
     */

    private int status;

    /**
     * unconnect,connecting,connected,disconnect, fail,available
     */


    private int connectStatus;

    private Channel channel;

    private EventListener registerListener;
    private EventListener connectedListener;
    private EventListener disconnectListener;

    private String remoteVersion;

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint32();
        s += SerializeUtils.sizeOfUint16();
        s += SerializeUtils.sizeOfString(ip);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(magicNumber);
        stream.writeUint16(port);
        stream.writeString(ip);
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        magicNumber = buffer.readUint32();
        port = buffer.readUint16();
        ip = buffer.readString();
    }

    public Node() {
        this.status = NodeStatusEnum.UNCHECK;
        this.connectStatus = NodeConnectStatusEnum.UNCONNECT;
    }

    public Node(String ip, int port, int type) {
        this();
        this.ip = ip;
        this.port = port;
        this.type = type;
    }

    public Node(String ip, int port, int severPort, int type) {
        this(ip, port, type);
    }

    public Node(String id, String ip, int port, int serverPort, int type) {
        this(ip, port, serverPort, type);
        this.id = id;
    }

    public void destroy() {
        this.lastFailTime = TimeService.currentTimeMillis();
        this.channel = null;
    }

    public boolean isHandShake() {
        return this.status == NodeConnectStatusEnum.AVAILABLE;
    }

    public boolean isAlive() {
        return this.status == NodeConnectStatusEnum.CONNECTED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("id:" + getId() + ",");
        sb.append("type:" + type + ",");
        sb.append("status:" + status + ",");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        Node other = (Node) obj;
        if (StringUtils.isBlank(other.getId())) {
            return false;
        }
        return other.getId().equals(this.getId());
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long lastTime) {
        this.lastTime = lastTime;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
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

    public String getId() {
        return ip + ":" + port;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getBestBlockHeight() {
        return bestBlockHeight;
    }

    public NulsDigestData getBestBlockHash() {
        return bestBlockHash;
    }

    public void setBestBlockHeight(Long bestBlockHeight) {
        this.bestBlockHeight = bestBlockHeight;
    }

    public void setBestBlockHash(NulsDigestData bestBlockHash) {
        this.bestBlockHash = bestBlockHash;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(int connectStatus) {
        this.connectStatus = connectStatus;
    }

    public boolean isSeedNode() {
        return isSeedNode;
    }

    public void setSeedNode(boolean seedNode) {
        isSeedNode = seedNode;
    }

    public EventListener getRegisterListener() {
        return registerListener;
    }

    public void setRegisterListener(EventListener registerListener) {
        this.registerListener = registerListener;
    }

    public EventListener getConnectedListener() {
        return connectedListener;
    }

    public void setConnectedListener(EventListener connectedListener) {
        this.connectedListener = connectedListener;
    }

    public EventListener getDisconnectListener() {
        return disconnectListener;
    }

    public void setDisconnectListener(EventListener disconnectListener) {
        this.disconnectListener = disconnectListener;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public Long getTimeOffset() {
        return timeOffset;
    }

    public void setTimeOffset(Long timeOffset) {
        this.timeOffset = timeOffset;
    }
}
