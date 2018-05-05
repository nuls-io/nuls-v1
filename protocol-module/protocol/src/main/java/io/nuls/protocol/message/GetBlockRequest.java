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
 */
package io.nuls.protocol.message;

import io.nuls.protocol.constant.ProtocolEventType;
import io.nuls.protocol.message.base.NoticeData;
import io.nuls.protocol.model.GetBlockDataParam;
import io.nuls.kernel.model.NulsDigestData;

/**
 * get block by height.
 *
 * @author Niels
 * @date 2017/11/13
 */
public class GetBlockRequest extends BaseProtocolMessage<GetBlockDataParam> {

    public GetBlockRequest() {
        super(ProtocolEventType.GET_BLOCK);
    }

    public GetBlockRequest(long start, long size) {
        this();
        GetBlockDataParam param = new GetBlockDataParam();
        param.setSize(size);
        param.setStart(start);
        this.setMsgBody(param);
    }

    public GetBlockRequest(long start, long size, NulsDigestData startHash, NulsDigestData endHash) {
        this();
        GetBlockDataParam param = new GetBlockDataParam();
        param.setSize(size);
        param.setStart(start);
        param.setStartHash(startHash);
        param.setEndHash(endHash);
        this.setMsgBody(param);
    }

    @Override
    public NoticeData getNotice() {
        return null;
    }

    public long getStart() {
        if (null == this.getMsgBody()) {
            return -1;
        }
        return this.getMsgBody().getStart();
    }

    public long getSize() {
        if (null == this.getMsgBody()) {
            return -1;
        }
        return this.getMsgBody().getSize();
    }


    public NulsDigestData getStartHash() {
        if (null == this.getMsgBody()) {
            return null;
        }
        return this.getMsgBody().getStartHash();
    }

    public NulsDigestData getEndHash() {
        if (null == this.getMsgBody()) {
            return null;
        }
        return this.getMsgBody().getEndHash();
    }

}
