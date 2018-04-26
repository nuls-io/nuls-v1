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
package io.nuls.network.message.entity;

import io.nuls.core.cfg.NulsConfig;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.protocol.context.NulsContext;
import io.nuls.protocol.event.base.BaseEvent;
import io.nuls.protocol.event.base.EventHeader;
import io.nuls.protocol.event.base.NoticeData;
import io.nuls.protocol.model.BaseNulsData;
import io.nuls.protocol.utils.io.NulsByteBuffer;
import io.nuls.protocol.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author vivi
 * @Date 2017.11.01
 */
public class VersionEvent extends BaseEvent {

    private int severPort;

    private long bestBlockHeight;

    private String bestBlockHash;

    private String nulsVersion;


    public VersionEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_VERSION_EVENT);
    }

    public VersionEvent(int severPort, long bestBlockHeight, String bestBlockHash) {
        this();
        this.severPort = severPort;
        this.bestBlockHeight = bestBlockHeight;
        this.bestBlockHash = bestBlockHash;
        this.nulsVersion = NulsConfig.VERSION;
    }


    @Override
    public int size() {
        int s = 0;
        s += EventHeader.EVENT_HEADER_LENGTH;
        s += VarInt.sizeOf(severPort);
        s += VarInt.sizeOf(bestBlockHeight);
        s += Utils.sizeOfString(bestBlockHash);
        s += Utils.sizeOfString(nulsVersion);
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(getHeader());
        stream.writeVarInt(severPort);
        stream.writeVarInt(bestBlockHeight);
        stream.writeString(bestBlockHash);
        stream.writeString(nulsVersion);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.setHeader(byteBuffer.readNulsData(new EventHeader()));
        severPort = (int) byteBuffer.readVarInt();
        bestBlockHeight = byteBuffer.readVarInt();
        bestBlockHash = byteBuffer.readString();
        nulsVersion = byteBuffer.readString();
    }

    @Override
    protected BaseNulsData parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("versionEvent:{");
        buffer.append(getHeader().toString());
        buffer.append("severPort:" + severPort + ", ");
        buffer.append("bestBlockHeight:" + bestBlockHeight + ", ");
        buffer.append("bestBlockHash:" + bestBlockHash + ", ");
        buffer.append("nulsVersion:" + nulsVersion + "}");

        return buffer.toString();
    }

    public long getBestBlockHeight() {
        return bestBlockHeight;
    }

    public void setBestBlockHeight(long bestBlockHeight) {
        this.bestBlockHeight = bestBlockHeight;
    }

    public String getBestBlockHash() {
        return bestBlockHash;
    }

    public void setBestBlockHash(String bestBlockHash) {
        this.bestBlockHash = bestBlockHash;
    }

    public String getNulsVersion() {
        return nulsVersion;
    }

    public void setNulsVersion(String nulsVersion) {
        this.nulsVersion = nulsVersion;
    }

    public int getSeverPort() {
        return severPort;
    }

    public void setSeverPort(int severPort) {
        this.severPort = severPort;
    }


}
