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

package io.nuls.network.protocol.message;

import io.nuls.kernel.cfg.NulsConfig;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.func.TimeService;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;
import io.nuls.kernel.utils.VarInt;
import io.protostuff.Tag;

import java.io.IOException;

public class NetworkMessageBody extends BaseNulsData {

    private int handshakeType;

    private int severPort;

    private long bestBlockHeight;

    private NulsDigestData bestBlockHash;

    private long networkTime;

    private String nodeIp;

    private String version;

    public NetworkMessageBody() {
        this.version = NulsConfig.VERSION;
    }

    public NetworkMessageBody(int handshakeType, int severPort, long bestBlockHeight, NulsDigestData bestBlockHash) {
        this.handshakeType = handshakeType;
        this.severPort = severPort;
        this.bestBlockHeight = bestBlockHeight;
        this.bestBlockHash = bestBlockHash;
        this.networkTime = TimeService.currentTimeMillis();
        this.version = NulsConfig.VERSION;
    }

    public NetworkMessageBody(int handshakeType, int severPort, long bestBlockHeight, NulsDigestData bestBlockHash, String ip) {
        this(handshakeType, severPort, bestBlockHeight, bestBlockHash);
        this.nodeIp = ip;
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint16(); // handshakeType
        s += SerializeUtils.sizeOfUint16(); // severPort
        s += SerializeUtils.sizeOfUint32(); // bestBlockHeight
        s += bestBlockHash.size();
        s += SerializeUtils.sizeOfUint48(); // networkTime
        s += SerializeUtils.sizeOfString(nodeIp);
        s += SerializeUtils.sizeOfString(version);
        return s;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(handshakeType);
        stream.writeUint16(severPort);
        stream.writeUint32(bestBlockHeight);
        stream.write(bestBlockHash.serialize());
        stream.writeUint48(networkTime);
        stream.writeString(nodeIp);
        stream.writeString(version);
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        handshakeType = buffer.readUint16();
        severPort = buffer.readUint16();
        bestBlockHeight = buffer.readUint32();
        bestBlockHash = buffer.readHash();
        networkTime = buffer.readUint48();
        nodeIp = buffer.readString();
        version = buffer.readString();
    }

    public int getHandshakeType() {
        return handshakeType;
    }

    public void setHandshakeType(int handshakeType) {
        this.handshakeType = handshakeType;
    }

    public int getSeverPort() {
        return severPort;
    }

    public void setSeverPort(int severPort) {
        this.severPort = severPort;
    }

    public long getBestBlockHeight() {
        return bestBlockHeight;
    }

    public void setBestBlockHeight(long bestBlockHeight) {
        this.bestBlockHeight = bestBlockHeight;
    }

    public NulsDigestData getBestBlockHash() {
        return bestBlockHash;
    }

    public void setBestBlockHash(NulsDigestData bestBlockHash) {
        this.bestBlockHash = bestBlockHash;
    }

    public long getNetworkTime() {
        return networkTime;
    }

    public void setNetworkTime(long networkTime) {
        this.networkTime = networkTime;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public static void main(String[] args) throws IOException, NulsException {
        NetworkMessageBody networkMessageBody = new NetworkMessageBody();
        networkMessageBody.setSeverPort(1001);
        networkMessageBody.setNetworkTime(20003L);
        networkMessageBody.setHandshakeType(1);
        networkMessageBody.setBestBlockHeight(4003L);
        networkMessageBody.setBestBlockHash(new NulsDigestData());

        byte[] bytes = networkMessageBody.serialize();
        NetworkMessageBody n2 = new NetworkMessageBody();
        n2.parse(bytes,0);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
