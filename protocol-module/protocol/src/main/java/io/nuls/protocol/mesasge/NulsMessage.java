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
 *
 */
package io.nuls.protocol.mesasge;


import io.nuls.kernel.constant.ErrorCode;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsVerificationException;
import io.nuls.protocol.event.base.BaseEvent;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

public class NulsMessage<T extends BaseEvent> {

    private static transient RuntimeSchema schema = RuntimeSchema.createFrom(NulsMessage.class);

    protected NulsMessageHeader header;

    protected T data;

    public NulsMessage() {
        this.header = new NulsMessageHeader();
    }

    public NulsMessage(byte[] bytes) throws NulsException {
        parse(bytes);
    }

    public NulsMessage(NulsMessageHeader header) {
        this.header = header;
    }

    public NulsMessage(NulsMessageHeader header, T data) {
        this.header = header;
        this.data = data;
        caculateXor();
        header.setLength(data.size());
    }

    public NulsMessage(T data) {
        this.header = new NulsMessageHeader();
        this.data = data;
        caculateXor();
        header.setLength(data.size());
    }

    public NulsMessage(int magicNumber) {
        this();
        this.header.setMagicNumber(magicNumber);
    }

    public NulsMessage(int magicNumber, T data) {
        this(data);
        this.header.setMagicNumber(magicNumber);
    }

    public NulsMessageHeader getHeader() {
        return header;
    }

    public byte caculateXor() {
        byte[] stream = data.serialize();
        if (header == null || stream == null || stream.length == 0) {
            return 0x00;
        }
        byte xor = 0x00;
        for (int i = 0; i < stream.length; i++) {
            xor ^= stream[i];
        }
        header.setXor(xor);
        return xor;
    }

    public byte[] serialize() {
        return ProtostuffIOUtil.toByteArray(this, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
    }

    public void parse(byte[] bytes) {
        ProtostuffIOUtil.mergeFrom(bytes, this, schema);
    }

    public void setHeader(NulsMessageHeader header) {
        this.header = header;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void verify() throws NulsVerificationException {
        if (this.header == null || this.data == null) {
            throw new NulsVerificationException(KernelErrorCode.NET_MESSAGE_ERROR);
        }

        if (header.getLength() != data.size()) {
            throw new NulsVerificationException(KernelErrorCode.NET_MESSAGE_LENGTH_ERROR);
        }

        if (header.getXor() != caculateXor()) {
            throw new NulsVerificationException(KernelErrorCode.NET_MESSAGE_XOR_ERROR);
        }
    }

}
