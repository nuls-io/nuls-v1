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
package io.nuls.core.tools.param;


import io.nuls.core.tools.str.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/9/29
 */
public final class AssertUtil {

    public static void isEquals(Object val1, Object val2, String msg) {
        if (val1 == val2 || (val1 != null && val1.equals(val2))) {
            return;
        }
        throw new RuntimeException(msg);
    }

    public static void canNotEmpty(Object val) {
        canNotEmpty(val, "null parameter");
    }

    public static void canNotEmpty(Object val, String msg) {
        boolean b = false;
        do {
            if (null == val) {
                b = true;
                break;
            }
            if (val instanceof String) {
                b = StringUtils.isBlank(val + "");
                break;
            }
            if (val instanceof List) {
                b = ((List) val).isEmpty();
                break;
            }
            if (val instanceof Map) {
                b = ((Map) val).isEmpty();
                break;
            }
            if (val instanceof String[]) {
                b = ((String[]) val).length == 0;
                break;
            }
            if (val instanceof byte[]) {
                b = ((byte[]) val).length == 0;
                break;
            }
        } while (false);
        if (b) {
            throw new RuntimeException(msg);
        }
    }
}
