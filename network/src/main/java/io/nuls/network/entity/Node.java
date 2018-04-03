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
import io.nuls.core.context.NulsContext;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.date.DateUtil;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;
import io.nuls.core.utils.str.StringUtils;
import io.nuls.network.entity.param.AbstractNetworkParam;
import io.nuls.network.message.entity.VersionEvent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class Node extends BaseNulsData {

    private int magicNumber;

    private String channelId;

    private String id;

    private String ip;

    private Integer port;

    private Integer severPort;

    private Long lastTime;

    private Long lastFailTime;

    private Integer failCount;

    private Set<String> groupSet;

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
    public final static int CONNECT = 1;
    public final static int HANDSHAKE = 2;
    public final static int CLOSE = 3;
    private volatile int status;

    private VersionEvent versionMessage;

    public Node() {
        super();
    }

    public Node(AbstractNetworkParam network) {
        this();
        this.magicNumber = network.packetMagic();
        this.groupSet = ConcurrentHashMap.newKeySet();
    }

    public Node(AbstractNetworkParam network, int type) {
        this(network);
        this.type = type;
    }

    public Node(AbstractNetworkParam network, int type, String ip, int port, String channelId) {
        this(network, type);
        this.port = port;
        this.ip = ip;
        this.channelId = channelId;
    }

    public Node(AbstractNetworkParam network, int type, InetSocketAddress socketAddress) {
        this(network, type);
        this.port = socketAddress.getPort();
        this.ip = socketAddress.getHostString();
    }

    public void destroy() {
        this.lastFailTime = TimeService.currentTimeMillis();
        this.setFailCount(this.getFailCount() + 1);
        this.status = Node.CLOSE;
    }

    @Override
    public int size() {
        int s = 0;
        s += VarInt.sizeOf(magicNumber);
        s += VarInt.sizeOf(port);
        s += 1;
        try {
            s += ip.getBytes(NulsContext.DEFAULT_ENCODING).length;
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
        }
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(new VarInt(magicNumber).encode());
        stream.write(new VarInt(port).encode());
        stream.writeString(ip);
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        magicNumber = (int) buffer.readVarInt();
        port = (int) buffer.readVarInt();
        ip = new String(buffer.readByLengthByte());
        this.groupSet = ConcurrentHashMap.newKeySet();
    }

    public boolean isHandShake() {
        return this.status == Node.HANDSHAKE;
    }

    public boolean isAlive() {
        return this.status == Node.CONNECT || status == Node.HANDSHAKE;
    }

    public void addToGroup(NodeGroup nodeGroup) {
        if (nodeGroup != null) {
            this.groupSet.add(nodeGroup.getName());
        }
    }

    public void removeFromGroup(NodeGroup nodeGroup) {
        if (nodeGroup != null) {
            this.groupSet.remove(nodeGroup.getName());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("ip: '" + getIp() + "',");
        sb.append("port: " + getPort() + ",");
        if (lastTime == null) {
            lastTime = System.currentTimeMillis();
        }

        sb.append("lastTime: " + DateUtil.convertDate(new Date(lastTime)) + ",");
        sb.append("magicNumber: " + magicNumber + "}");
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

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
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

    public int getGroupCount(String groupName) {
        return this.groupSet.size();
    }

    public Set<String> getGroupSet() {
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

    public String getId() {
        if (StringUtils.isBlank(id)) {
            id = ip + ":" + port;
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getSeverPort() {
        if(severPort == null) {
            severPort = 0;
        }
        return severPort;
    }

    public void setSeverPort(Integer severPort) {
        this.severPort = severPort;
    }
}
