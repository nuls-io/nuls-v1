/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.consensus.poc.storage.po;

import io.nuls.core.tools.array.ArraysTool;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.BaseNulsData;
import io.nuls.kernel.utils.*;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Niels
 * @date 2018/3/22
 */
public class PunishLogPo extends BaseNulsData {
    private byte type;
    private byte[] address;
    private long time;
    private long height;
    private long roundIndex;
    private short reasonCode;
    private byte[] evidence;
    private int index;

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(type);
        stream.write(address);
        stream.writeUint48(time);
        stream.writeVarInt(height);
        stream.writeVarInt(roundIndex);
        stream.writeShort(reasonCode);
        stream.writeBytesWithLength(evidence);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.type = byteBuffer.readByte();
        this.address = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.time = byteBuffer.readUint48();
        this.height = byteBuffer.readVarInt();
        this.roundIndex = byteBuffer.readVarInt();
        this.reasonCode = byteBuffer.readShort();
        this.evidence = byteBuffer.readByLengthByte();
    }

    @Override
    public int size() {
        int size = 0;
        size += 1;
        size += Address.ADDRESS_LENGTH;
        size += SerializeUtils.sizeOfUint48();
        size += SerializeUtils.sizeOfVarInt(height);
        size += SerializeUtils.sizeOfVarInt(roundIndex);
        size += 2;
        size += SerializeUtils.sizeOfBytes(this.evidence);
        return size;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }

    public int getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getKey() {
        return ArraysTool.concatenate(address, new byte[]{type}, SerializeUtils.uint64ToByteArray(height), new VarInt(index).encode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PunishLogPo)) {
            return false;
        }
        return Arrays.equals(this.getKey(), ((PunishLogPo) obj).getKey());
    }
}
