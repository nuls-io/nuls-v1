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

package io.nuls.core.tools.str;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Niels
 */
public class VersionUtils {

    /**
     * @param version0 main version
     * @param version1 other version
     * @return boolean
     */
    public static boolean higherThan(String version0, String version1) {
        if (StringUtils.isBlank(version0) || StringUtils.isBlank(version1)) {
            throw new RuntimeException("version is null");
        }
        Integer[] intArr0 = strArrayToInt(version0);
        Integer[] intArr1 = strArrayToInt(version1);
        boolean result = false;
        for (int i = 0; i < intArr0.length; i++) {
            Integer val1 = intArr0[i];
            if (intArr1.length <= i && val1 > 0) {
                result = true;
                break;
            }
            Integer val2 = intArr1[i];
            if (val1 > val2) {
                result = true;
                break;
            }
        }
        //当version1版本号位数更多时
        if (!result && intArr1.length > intArr0.length) {
            result = intArr1[intArr0.length] > 0;
        }
        return result;
    }

    public static boolean equalsWith(String version0, String version1) {
        if (StringUtils.isBlank(version0) || StringUtils.isBlank(version1)) {
            throw new RuntimeException("version is null");
        }
        Integer[] intArr0 = strArrayToInt(version0);
        Integer[] intArr1 = strArrayToInt(version1);
        boolean result = intArr0.length == intArr1.length;
        if (!result) {
            return false;
        }
        for (int i = 0; i < intArr0.length; i++) {
            Integer val1 = intArr0[i];
            Integer val2 = intArr1[i];
            if (val1 != val2) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param version0 main version
     * @param version1 other version
     * @return boolean
     */
    public static boolean lowerThan(String version0, String version1) {
        if (StringUtils.isBlank(version0) || StringUtils.isBlank(version1)) {
            throw new RuntimeException("version is null");
        }
        Integer[] intArr0 = strArrayToInt(version0);
        Integer[] intArr1 = strArrayToInt(version1);
        boolean result = false;
        for (int i = 0; i < intArr0.length; i++) {
            Integer val1 = intArr0[i];
            if (intArr1.length <= i && val1 > 0) {
                break;
            }
            Integer val2 = intArr1[i];
            if (val1 < val2) {
                result = true;
                break;
            }
        }
        if (!result && intArr1.length > intArr0.length && intArr1[intArr0.length] > 0) {
            result = true;
        }
        return result;
    }

    private static Integer[] strArrayToInt(String version) {
        if (StringUtils.isBlank(version)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(version);
        List<Integer> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(Integer.parseInt(matcher.group(0)));
        }
        return list.toArray(new Integer[list.size()]);
    }

}
