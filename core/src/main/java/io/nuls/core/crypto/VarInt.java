/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nuls.core.crypto;


import io.nuls.core.utils.crypto.Utils;

/**
 * A variable-length encoded unsigned integer using Satoshi's encoding (a.k.a. "CompactSize").
 */
public class VarInt {
    public final long value;
    private final int originallyEncodedSize;

    /**
     * Constructs a new VarInt with the given unsigned long value.
     *
     * @param value the unsigned long value (beware widening conversion of negatives!)
     */
    public VarInt(long value) {
        this.value = value;
        originallyEncodedSize = getSizeInBytes();
    }

    /**
     * Constructs a new VarInt with the value parsed from the specified offset of the given buffer.
     *
     * @param buf    the buffer containing the value
     * @param offset the offset of the value
     */
    public VarInt(byte[] buf, int offset) {
        int first = 0xFF & buf[offset];
        if (first < 253) {
            value = first;
            // 1 data byte (8 bits)
            originallyEncodedSize = 1;
        } else if (first == 253) {
            value = (0xFF & buf[offset + 1]) | ((0xFF & buf[offset + 2]) << 8);
            // 1 marker + 2 data bytes (16 bits)
            originallyEncodedSize = 3;
        } else if (first == 254) {
            value = Utils.readUint32(buf, offset + 1);
            // 1 marker + 4 data bytes (32 bits)
            originallyEncodedSize = 5;
        } else {
            value = Utils.readInt64(buf, offset + 1);
            // 1 marker + 8 data bytes (64 bits)
            originallyEncodedSize = 9;
        }
    }

    /**
     * Returns the original number of bytes used to encode the value if it was
     * deserialized from a byte array, or the minimum encoded size if it was not.
     */
    public int getOriginalSizeInBytes() {
        return originallyEncodedSize;
    }

    /**
     * Returns the minimum encoded size of the value.
     */
    public final int getSizeInBytes() {
        return sizeOf(value);
    }

    /**
     * Returns the minimum encoded size of the given unsigned long value.
     *
     * @param value the unsigned long value (beware widening conversion of negatives!)
     */
    public static int sizeOf(long value) {
        // if negative, it's actually a very large unsigned long value
        if (value < 0) {
            // 1 marker + 8 data bytes
            return 9;
        }
        if (value < 253) {
            // 1 data byte
            return 1;
        }
        if (value <= 0xFFFFL) {
            // 1 marker + 2 data bytes
            return 3;
        }
        if (value <= 0xFFFFFFFFL) {
            // 1 marker + 4 data bytes
            return 5;
        }
        // 1 marker + 8 data bytes
        return 9;
    }

    /**
     * Encodes the value into its minimal representation.
     *
     * @return the minimal encoded bytes of the value
     */
    public byte[] encode() {
        byte[] bytes;
        switch (sizeOf(value)) {
            case 1:
                return new byte[]{(byte) value};
            case 3:
                return new byte[]{(byte) 253, (byte) (value), (byte) (value >> 8)};
            case 5:
                bytes = new byte[5];
                bytes[0] = (byte) 254;
                Utils.uint32ToByteArrayLE(value, bytes, 1);
                return bytes;
            default:
                bytes = new byte[9];
                bytes[0] = (byte) 255;
                Utils.uint64ToByteArrayLE(value, bytes, 1);
                return bytes;
        }
    }
}
