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

import io.nuls.core.utils.crypto.Utils;
import org.junit.Test;

/**
 * @author Niels
 * @date 2018/3/14
 */
public class TimeTest {

    @Test
    public void test( ) {
        long time = System.currentTimeMillis();
        System.out.println(time);
        byte[] array = getBytes(time);
        long newTime = readTime(array);
        System.out.println(newTime);
    }

    private byte[] getBytes(long time) {
        byte[] bytes = new byte[Utils.sizeOfInt6()];
        bytes[0] = (byte) (0xFF & time);
        bytes[1] = (byte) (0xFF & (time >> 8));
        bytes[2] = (byte) (0xFF & (time >> 16));
        bytes[3] = (byte) (0xFF & (time >> 24));
        bytes[4] = (byte) (0xFF & (time >> 32));
        bytes[5] = (byte) (0xFF & (time >> 40));
        return bytes;
    }

    private long readTime(byte[] bytes) {
        long value = (bytes[0] & 0xffL) |
                ((bytes[1] & 0xffL) << 8) |
                ((bytes[2] & 0xffL) << 16) |
                ((bytes[3] & 0xffL) << 24) |
                ((bytes[4] & 0xffL) << 32) |
                ((bytes[5] & 0xffL) << 40);
        return value;
    }
}
