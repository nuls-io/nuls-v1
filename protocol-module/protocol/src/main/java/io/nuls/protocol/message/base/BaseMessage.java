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

import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsDigestData;
import io.protostuff.Tag;

/**
 * 所有网络上传输的消息的基类，定义了网络消息的基本格式
 * The base class for all messages transmitted over the network defines the basic format of the network message.
 *
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseMessage<T extends BaseNulsData> extends BaseNulsData {
    private transient NulsDigestData hash;

    @Tag(1)
    private MessageHeader header;

    @Tag(2)
    private T msgBody;

    /**
     * 初始化基础消息的消息头
     */
    public BaseMessage(short moduleId, short eventType) {
        this.header = new MessageHeader(moduleId, eventType);
    }

    /**
     * 计算msgBody的验证值，通过简单的异或得出结果，将结果放入消息头中
     * The verification value of msgBody is calculated,
     * and the result is put into the message header through a simple difference or result.
     * @return 验证值（计算结果）,Verification value (calculation result)
     */
    public byte caculateXor() {
        if (header == null || msgBody == null) {
            return 0x00;
        }
        byte xor = 0x00;
        byte[] data = msgBody.serialize();
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
            this.hash = NulsDigestData.calcDigestData(this.serialize());
        }
        return hash;
    }

    public abstract NoticeData getNotice();
}
