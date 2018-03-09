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
package io.nuls.core.mesasge;


import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsVerificationException;
import io.nuls.core.utils.io.NulsByteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NulsMessage {


    protected NulsMessageHeader header;

    protected byte[] data;

    public NulsMessage() {
        this.header = new NulsMessageHeader();
        this.data = new byte[0];
    }

    public NulsMessage(ByteBuffer buffer) throws NulsException {
        parse(buffer);
    }

    public NulsMessage(NulsMessageHeader header) {
        this.header = header;
        this.data = new byte[0];
    }

    public NulsMessage(NulsMessageHeader header, byte[] data) {
        this.header = header;
        this.data = data;
        caculateXor();
        header.setLength(data.length);
    }

    public NulsMessage(byte[] data) {
        this.header = new NulsMessageHeader();
        this.data = data;
        caculateXor();
        header.setLength(data.length);
    }

    public NulsMessage(int magicNumber) {
        this();
        this.header.setMagicNumber(magicNumber);
    }

    public NulsMessage(int magicNumber, byte[] data) {
        this(data);
        this.header.setMagicNumber(magicNumber);
    }

    public NulsMessageHeader getHeader() {
        return header;
    }

    public byte caculateXor() {
        if (header == null || data == null || data.length == 0) {
            return 0x00;
        }
        byte xor = 0x00;
        for (int i = 0; i < data.length; i++) {
            xor ^= data[i];
        }
        header.setXor(xor);
        return xor;
    }

    public byte[] serialize() throws IOException {
        byte[] value = new byte[NulsMessageHeader.MESSAGE_HEADER_SIZE + data.length];
        byte[] headerBytes = header.serialize();
        System.arraycopy(headerBytes, 0, value, 0, headerBytes.length);
        System.arraycopy(data, 0, value, headerBytes.length, data.length);
        return value;
    }

    public void parse(ByteBuffer byteBuffer) throws NulsException {
        byte[] headers = new byte[NulsMessageHeader.MESSAGE_HEADER_SIZE];
        byteBuffer.get(headers, 0, headers.length);
        NulsMessageHeader header = new NulsMessageHeader(new NulsByteBuffer(headers));
        byte[] data = new byte[header.getLength()];
        byteBuffer.get(data, 0, data.length);
        this.header = header;
        this.data = data;
    }

    public void setHeader(NulsMessageHeader header) {
        this.header = header;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void verify() throws NulsVerificationException {
        if (this.header == null || this.data == null) {
            throw new NulsVerificationException(ErrorCode.NET_MESSAGE_ERROR);
        }

        if (header.getLength() != data.length) {
            throw new NulsVerificationException(ErrorCode.NET_MESSAGE_LENGTH_ERROR);
        }

        if (header.getXor() != caculateXor()) {
            throw new NulsVerificationException(ErrorCode.NET_MESSAGE_XOR_ERROR);
        }
    }

}
