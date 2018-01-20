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
package io.nuls.consensus.event;

import io.nuls.consensus.constant.PocConsensusConstant;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.constant.NulsConstant;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.event.BaseEvent;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * @author Niels
 * @date 2018/1/15
 */
public class GetBlocksHashRequest extends BaseEvent<BasicTypeData<byte[]>> {
    private long start;
    private long end;
    private long split;

    public GetBlocksHashRequest() {
        super(NulsConstant.MODULE_ID_CONSENSUS, PocConsensusConstant.EVENT_TYPE_GET_BLOCKS_HASH);
    }

    public GetBlocksHashRequest(long start, long end, long split) {
        this();
        setRequestParams(start, end, split);
    }

    @Override
    protected BasicTypeData<byte[]> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        BasicTypeData<byte[]> data = byteBuffer.readNulsData(new BasicTypeData<>());
        NulsByteBuffer buffer = new NulsByteBuffer(data.getVal());
        this.start = buffer.readVarInt();
        this.end = buffer.readVarInt();
        this.split = buffer.readVarInt();
        return data;
    }

    public void setRequestParams(long start, long end, long split) {
        this.start = start;
        this.end = end;
        this.split = split;
        byte[] bytes1 = new VarInt(start).encode();
        byte[] bytes2 = new VarInt(end).encode();
        byte[] bytes3 = new VarInt(split).encode();
        byte[] bytes = new byte[bytes1.length + bytes2.length + bytes3.length];
        System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
        System.arraycopy(bytes3, 0, bytes, bytes2.length + bytes1.length, bytes3.length);
        BasicTypeData<byte[]> data = new BasicTypeData<>(bytes);
        this.setEventBody(data);
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getSplit() {
        return split;
    }
}
