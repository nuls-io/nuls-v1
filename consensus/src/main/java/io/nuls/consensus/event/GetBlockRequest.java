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
package io.nuls.consensus.event;

import io.nuls.consensus.constant.ConsensusEventType;
import io.nuls.core.chain.entity.BasicTypeData;
import io.nuls.core.crypto.VarInt;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.io.NulsByteBuffer;

/**
 * get block by height.
 *
 * @author Niels
 * @date 2017/11/13
 */
public class GetBlockRequest extends BaseConsensusEvent<BasicTypeData<byte[]>> {
    private long start;
    private long end;

    public GetBlockRequest() {
        super(ConsensusEventType.GET_BLOCK);
    }

    public GetBlockRequest(long start, long end) {
        this();
        byte[] bytes1 = new VarInt(start).encode();
        byte[] bytes2 = new VarInt(end).encode();
        byte[] bytes = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
        BasicTypeData<byte[]> data = new BasicTypeData<>(bytes);
        this.setEventBody(data);
    }

    @Override
    protected BasicTypeData<byte[]> parseEventBody(NulsByteBuffer byteBuffer) throws NulsException {
        BasicTypeData<byte[]> data = byteBuffer.readNulsData(new BasicTypeData<>());
        NulsByteBuffer buffer = new NulsByteBuffer(data.getVal());
        this.start = buffer.readVarInt();
        this.end = buffer.readVarInt();
        return data;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
