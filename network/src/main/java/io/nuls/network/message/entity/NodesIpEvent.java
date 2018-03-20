/**
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
 */
package io.nuls.network.message.entity;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.event.EventHeader;
import io.nuls.core.event.NoticeData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.network.constant.NetworkConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vivi
 * @date 2017/12/1.
 */
public class NodesIpEvent extends BaseEvent {

    private List<String> ipList;

    public NodesIpEvent() {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_NODEIP_EVENT);
        this.ipList = new ArrayList<>();
    }

    public NodesIpEvent(List ipList) {
        super(NulsConstant.MODULE_ID_NETWORK, NetworkConstant.NETWORK_GET_NODEIP_EVENT);
        this.ipList = ipList;
    }

    @Override
    public int size() {
        int s = 0;
        s += EventHeader.EVENT_HEADER_LENGTH;
        s += VarInt.sizeOf(ipList.size());
        for (String ip : ipList) {
            s += Utils.sizeOfString(ip);
        }
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(getHeader());
        stream.write(new VarInt(ipList.size()).encode());
        for (String ip : ipList) {
            stream.writeString(ip);
        }
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.setHeader(byteBuffer.readNulsData(new EventHeader()));
        int size = (int) byteBuffer.readVarInt();
        for (int i = 0; i < size; i++) {
            String ip = byteBuffer.readString();
            ipList.add(ip);
        }
    }

    @Override
    protected BaseEvent parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        return null;
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }
}
