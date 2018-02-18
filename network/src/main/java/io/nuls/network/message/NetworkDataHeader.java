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
package io.nuls.network.message;

import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.io.NulsByteBuffer;
import io.nuls.core.utils.io.NulsOutputStreamBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author vivi
 * @date 2017/11/28.
 */
public class NetworkDataHeader {

    public static final int NETWORK_HEADER_SIZE = 10;

    public static final int EXTEND_LENGTH = 6;

    private short moduleId;

    private short type;

    private byte[] extend;

    public NetworkDataHeader() {

    }

    public NetworkDataHeader(NulsByteBuffer buffer) throws NulsException {
        this.parse(buffer);
    }

    public NetworkDataHeader(short moduleId, short type) {
        this.moduleId = moduleId;
        this.type = type;
        this.extend = new byte[EXTEND_LENGTH];
    }

    public NetworkDataHeader(short moduleId, short type, byte[] extend) {
        this.moduleId = moduleId;
        this.type = type;
        this.extend = extend;
    }

    public byte[] serialize() {
        byte[] header = new byte[NETWORK_HEADER_SIZE];
        Utils.uint16ToByteArrayLE(moduleId, header, 0);
        Utils.uint16ToByteArrayLE(type, header, 2);

        System.arraycopy(extend, 0, header, 4, 6);
        return header;
    }

    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(serialize());
    }

    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.moduleId = byteBuffer.readShort();
        this.type = byteBuffer.readShort();
        this.extend = byteBuffer.readBytes(EXTEND_LENGTH);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("networkHeader:{");
        buffer.append("moduleId:" + moduleId + ", ");
        buffer.append("type:" + type + ", ");
        buffer.append("extend:[");
        if (extend != null && extend.length > 0) {
            for (byte b : extend) {
                buffer.append(b + " ");
            }
        }
        buffer.append("]}");
        return buffer.toString();
    }

    public short getModuleId() {
        return moduleId;
    }

    public void setModuleId(short moduleId) {
        this.moduleId = moduleId;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }

}
