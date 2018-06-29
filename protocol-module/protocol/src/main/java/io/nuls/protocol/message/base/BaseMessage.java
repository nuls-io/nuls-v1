/**
 * MIT License
 * *
 * Copyright (c) 2017-2018 nuls.io
 * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.protocol.message.base;

import io.nuls.core.tools.log.Log;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * 所有网络上传输的消息的基类，定义了网络消息的基本格式
 * The base class for all messages transmitted over the network defines the basic format of the network message.
 *
 * @author Niels
 */
public abstract class BaseMessage<T extends BaseNulsData> extends BaseNulsData {

    private transient NulsDigestData hash;

    private MessageHeader header;

    private T msgBody;

    public BaseMessage() {

    }

    /**
     * 初始化基础消息的消息头
     */
    public BaseMessage(short moduleId, short msgType) {
        this.header = new MessageHeader(moduleId, msgType);
    }


    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(header.serialize());
        stream.write(msgBody.serialize());
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        MessageHeader header = new MessageHeader();
        header.parse(byteBuffer);
        this.header = header;
        this.msgBody = parseMessageBody(byteBuffer);
    }

    protected abstract T parseMessageBody(NulsByteBuffer byteBuffer) throws NulsException;

    @Override
    public int size() {
        int s = 0;
        s += header.size();
        s += msgBody.size();
        return s;
    }

    /**
     * 计算msgBody的验证值，通过简单的异或得出结果，将结果放入消息头中
     * The verification value of msgBody is calculated,
     * and the result is put into the message header through a simple difference or result.
     *
     * @return 验证值（计算结果）,Verification value (calculation result)
     */
    public byte caculateXor() {
        if (header == null || msgBody == null) {
            return 0x00;
        }
        byte xor = 0x00;
        byte[] data = new byte[0];
        try {
            data = msgBody.serialize();
        } catch (IOException e) {
            Log.error(e);
        }
        for (int i = 0; i < data.length; i++) {
            xor ^= data[i];
        }
        header.setXor(xor);
        return xor;
    }

    public T getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(T msgBody) {
        this.msgBody = msgBody;
    }

    public MessageHeader getHeader() {
        return header;
    }

    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    public NulsDigestData getHash() {
        if (hash == null) {
            try {
                this.hash = NulsDigestData.calcDigestData(this.serialize());
            } catch (IOException e) {
                Log.error(e);
            }
        }
        return hash;
    }

}
