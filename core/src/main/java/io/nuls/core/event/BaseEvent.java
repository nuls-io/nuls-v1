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
package io.nuls.core.event;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.intf.NulsCloneable;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;
import io.nuls.core.utils.log.Log;

import java.io.IOException;

/**
 * @author Niels
 * @date 2017/11/7
 */
public abstract class BaseEvent<T extends BaseNulsData> extends BaseNulsData implements NulsCloneable {
    private NulsDigestData hash;
    private EventHeader header;
    private T eventBody;
    private NulsSignData sign;

    public BaseEvent(short moduleId, short eventType) {
        this.header = new EventHeader(moduleId, eventType);
        //todo 临时处理事件签名
        sign = NulsSignData.EMPTY_SIGN;
    }

    @Override
    public int size() {
        int size = Utils.sizeOfSerialize(header);
        size += Utils.sizeOfSerialize(eventBody);
        size += Utils.sizeOfSerialize(sign);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(header);
        stream.writeNulsData(this.eventBody);
        stream.writeNulsData(sign);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.header = byteBuffer.readNulsData(new EventHeader());
        this.eventBody = parseEventBody(byteBuffer);
        try {
            this.hash = NulsDigestData.calcDigestData(this.serialize());
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(ErrorCode.DATA_PARSE_ERROR);
        }
        this.sign = byteBuffer.readSign();
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

    protected abstract T parseEventBody(NulsByteBuffer byteBuffer) throws NulsException;

    public T getEventBody() {
        return eventBody;
    }

    public void setEventBody(T eventBody) {
        this.eventBody = eventBody;
    }

    public EventHeader getHeader() {
        return header;
    }

    public void setHeader(EventHeader header) {
        this.header = header;
    }

    public NulsSignData getSign() {
        return sign;
    }

    public void setSign(NulsSignData sign) {
        this.sign = sign;
    }

    public NulsDigestData getHash() {
        if(hash==null){
            try {
                this.hash = NulsDigestData.calcDigestData(this.serialize());
            } catch (IOException e) {
                Log.error(e);
            }
        }
        return hash;
    }

}
