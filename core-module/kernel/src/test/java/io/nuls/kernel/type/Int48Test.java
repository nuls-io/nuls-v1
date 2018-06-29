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

package io.nuls.kernel.type;

import io.nuls.kernel.utils.SerializeUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Niels
 */
public class Int48Test {
    @Test
    public void test() {
        long time = -1L;
        byte[] bytes = SerializeUtils.uint48ToBytes(time);
        long value = readInt48(bytes);
//        assertEquals(value, time);
    }

    @Test
    public void testLong() {
        byte[] bytes = new byte[]{-1,-1,-1,-1,-1,-1,0,0};
        long value = SerializeUtils.readInt64LE(bytes,0);
        System.out.println(value);
    }

    private long readInt48(byte[] bytes) {
        long value = (bytes[0] & 0xffL) |
                ((bytes[1] & 0xffL) << 8) |
                ((bytes[2] & 0xffL) << 16) |
                ((bytes[3] & 0xffL) << 24) |
                ((bytes[4] & 0xffL) << 32) |
                ((bytes[5] & 0xffL) << 40) ;
        return value;
    }
}
