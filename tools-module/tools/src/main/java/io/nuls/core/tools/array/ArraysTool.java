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

package io.nuls.core.tools.array;

import java.util.Arrays;

/**
 * @author: Niels Wang
 */
public class ArraysTool {
    /**
     * 按照传入的顺序拼接数组为一个包含所有数组的大数组
     * Splices the array into a large array containing all of the arrays in the incoming order.
     *
     * @param arrays 想要拼接的数组集合、A collection of arrays that you want to concatenate.
     * @return 拼接结果、 the result of the Joining together
     */
    public static final byte[] concatenate(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] t = new byte[length];
        int offset = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, t, offset, array.length);
            offset += array.length;
        }
        return t;
    }

    public static final boolean isEmptyOrNull(byte[] bytes) {
        return (bytes == null || bytes.length == 0);
    }

    public static boolean arrayEquals(byte[] array1, byte[] array2) {
        return Arrays.equals(array1, array2);
    }
}
