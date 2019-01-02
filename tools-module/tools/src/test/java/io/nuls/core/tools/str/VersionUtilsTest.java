/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
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
 *  *
 *
 */

package io.nuls.core.tools.str;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 * @date: 2018/10/17
 */
public class VersionUtilsTest {

    @Test
    public void higherThan() {
        String v1 = "1.1.0";
        String v2 = "1.1.0-beta";
        String v3 = "1.1.1";
        String v4 = "1.1.1-beta1";
        String v5 = "1.1.1-beta2";
        String v6 = "1.1.2-beta1";
        String v7 = "1.2.0";

        assertTrue(!VersionUtils.higherThan(v2, v1));
        assertTrue(VersionUtils.higherThan(v3, v2));
        assertTrue(VersionUtils.higherThan(v4, v3));
        assertTrue(VersionUtils.higherThan(v5, v4));
        assertTrue(VersionUtils.higherThan(v6, v5));
        assertTrue(VersionUtils.higherThan(v7, v1));
        assertTrue(VersionUtils.higherThan(v7, v2));
        assertTrue(VersionUtils.higherThan(v7, v6));


        assertTrue(VersionUtils.lowerThan(v2, v3));
        assertTrue(VersionUtils.lowerThan(v3, v4));
        assertTrue(VersionUtils.lowerThan(v4, v5));
        assertTrue(VersionUtils.lowerThan(v5, v6));
        assertTrue(VersionUtils.lowerThan(v1, v7));
        assertTrue(VersionUtils.lowerThan(v2, v7));
        assertTrue(VersionUtils.lowerThan(v6, v7));
        assertTrue(!VersionUtils.lowerThan(v2, v1));


        assertTrue(VersionUtils.equalsWith(v1, v2));
    }
}