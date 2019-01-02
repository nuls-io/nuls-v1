/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.vm.code;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.List;

public class Descriptors {

    public static final BiMap<String, String> DESCRIPTORS;

    public static final String VOID = "void";
    public static final String BYTE = "byte";
    public static final String CHAR = "char";
    public static final String DOUBLE = "double";
    public static final String FLOAT = "float";
    public static final String INT = "int";
    public static final String LONG = "long";
    public static final String SHORT = "short";
    public static final String BOOLEAN = "boolean";

    public static final String DOUBLE_DESC = "D";
    public static final String LONG_DESC = "J";

    static {
        DESCRIPTORS = HashBiMap.create();
        DESCRIPTORS.put(VOID, "V");
        DESCRIPTORS.put(BYTE, "B");
        DESCRIPTORS.put(CHAR, "C");
        DESCRIPTORS.put(DOUBLE, DOUBLE_DESC);
        DESCRIPTORS.put(FLOAT, "F");
        DESCRIPTORS.put(INT, "I");
        DESCRIPTORS.put(LONG, LONG_DESC);
        DESCRIPTORS.put(SHORT, "S");
        DESCRIPTORS.put(BOOLEAN, "Z");
    }

    public static List<String> parse(String desc) {
        return parse(desc, false);
    }

    public static List<String> parse(String desc, boolean includeReturn) {
        boolean isL = false;
        boolean isEnd = false;
        StringBuilder sb = new StringBuilder();
        List<String> descList = new ArrayList<>();

        for (char c : desc.toCharArray()) {
            if ('[' == c) {
                sb.append(c);
            } else if ('(' == c) {
                //
            } else if (')' == c) {
                isEnd = true;
            } else if (';' == c) {
                isEnd = true;
                sb.append(c);
            } else if (isL) {
                sb.append(c);
            } else if ('L' == c) {
                isL = true;
                sb.append(c);
            } else if (DESCRIPTORS.inverse().containsKey(String.valueOf(c))) {
                isEnd = true;
                sb.append(c);
            } else {
                throw new IllegalArgumentException("unknown desc");
            }

            if (isEnd) {
                if (sb.length() > 0) {
                    descList.add(sb.toString());
                    sb = new StringBuilder();
                }
                isL = false;
                isEnd = false;
            }

            if (')' == c) {
                if (includeReturn) {
                    //
                } else {
                    break;
                }
            }

        }

        return descList;
    }

}
