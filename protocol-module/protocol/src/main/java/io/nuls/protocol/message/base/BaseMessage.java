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
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.model.NulsCloneable;
import io.nuls.kernel.model.NulsDigestData;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseMessage<T extends BaseNulsData> extends BaseNulsData implements NulsCloneable {
    private transient NulsDigestData hash;
    private MessageHeader header;
    private T msgBody;

    public BaseMessage(short moduleId, short eventType) {
        this.header = new MessageHeader(moduleId, eventType);
    }

    @Override
    public Object copy() {
        try {
            return this.clone();
        } catch (CloneNotSupportedException e) {
            Log.error(e);
            return null;
        }
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

    public byte caculateXor() {
        // todo auto-generated method stub(Niels)
        return 0;
    }
}
