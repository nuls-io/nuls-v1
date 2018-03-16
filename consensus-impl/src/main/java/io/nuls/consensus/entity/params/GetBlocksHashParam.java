/*
 *
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

package io.nuls.consensus.entity.params;

import io.nuls.core.chain.entity.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.date.TimeService;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;

/**
 * @author Niels
 * @date 2018/2/8
 */
public class GetBlocksHashParam extends BaseNulsData {
    private long time;
    private long start;
    private long end;
    private long split;

    public GetBlocksHashParam(){
        this.time = TimeService.currentTimeMillis();
    }
    @Override
    public int size() {
        int size = Utils.sizeOfInt6();
        size += Utils.sizeOfLong(start);
        size +=Utils.sizeOfLong(end);
        size += Utils.sizeOfLong(split);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
         stream.writeInt48(time);
         stream.writeVarInt(start);
         stream.writeVarInt(end);
         stream.writeVarInt(split);
    }

    @Override
    protected void parse(NulsByteBuffer byteBuffer) throws NulsException {
       this.time = byteBuffer.readInt48();
       this.start = byteBuffer.readVarInt();
       this.end = byteBuffer.readVarInt();
       this.split = byteBuffer.readVarInt();
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getSplit() {
        return split;
    }

    public void setSplit(long split) {
        this.split = split;
    }

    public static void main(String []args)throws Exception{
        GetBlocksHashParam param = new GetBlocksHashParam();
        param.parse(new NulsByteBuffer(Hex.decode("ffff1d50746101000000000100000100")));

    }
}
