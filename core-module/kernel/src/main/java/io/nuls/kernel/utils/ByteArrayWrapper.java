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
package io.nuls.kernel.utils;

import static java.util.Objects.requireNonNull;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/6/28
 */
public class ByteArrayWrapper implements Comparable<ByteArrayWrapper> {

    private final byte[] data;
    private final int offset;
    private final int length;

    private int hash;

    public ByteArrayWrapper(byte[] data) {
        requireNonNull(data, "array is null");
        this.data = data;
        this.offset = 0;
        this.length = data.length;
    }

    public byte[] getBytes() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ByteArrayWrapper byteArrayWrapper = (ByteArrayWrapper) o;

        // do lengths match
        if (length != byteArrayWrapper.length) {
            return false;
        }

        // if arrays have same base offset, some optimizations can be taken...
        if (offset == byteArrayWrapper.offset && data == byteArrayWrapper.data) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (data[offset + i] != byteArrayWrapper.data[byteArrayWrapper.offset + i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }

        int result = length;
        for (int i = offset; i < offset + length; i++) {
            result = 31 * result + data[i];
        }
        if (result == 0) {
            result = 1;
        }
        hash = result;
        return hash;
    }

    @Override
    public int compareTo(ByteArrayWrapper that) {
        if (this == that) {
            return 0;
        }
        if (this.data == that.data && length == that.length && offset == that.offset) {
            return 0;
        }

        int minLength = Math.min(this.length, that.length);
        for (int i = 0; i < minLength; i++) {
            int thisByte = 0xFF & this.data[this.offset + i];
            int thatByte = 0xFF & that.data[that.offset + i];
            if (thisByte != thatByte) {
                return (thisByte) - (thatByte);
            }
        }
        return this.length - that.length;
    }
}
