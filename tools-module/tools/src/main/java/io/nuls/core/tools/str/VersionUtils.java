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

package io.nuls.core.tools.str;

/**
 * @author Niels
 * @date 2018/3/5
 */
public class VersionUtils {

    /**
     * @param version0 main version
     * @param version1 other version
     */
    public static boolean higherThan(String version0, String version1) {
        if (StringUtils.isBlank(version0) || StringUtils.isBlank(version1)) {
            throw new RuntimeException("version is null");
        }
        version0 = version0.replace("-SNAPSHOT", "");
        version1 = version1.replace("-SNAPSHOT", "");
        String[] array0 = version0.split("\\.");
        String[] array1 = version1.split("\\.");
        if (array0.length != 3 || array1.length != 3) {
            throw new RuntimeException("version format is wrong");
        }
        int[] intArr0 = strArrayToInt(array0);
        int[] intArr1 = strArrayToInt(array1);
        boolean result = intArr0[0] > intArr1[0];
        if (result) {
            return true;
        }
        result = intArr0[0] == intArr1[0] && intArr0[1] > intArr1[1];
        if (result) {
            return true;
        }
        return intArr0[0] == intArr1[0] && intArr0[1] == intArr1[1] && intArr0[2] > intArr1[2];
    }

    public static boolean equalsWith(String version0, String version1) {
        if (StringUtils.isBlank(version0) || StringUtils.isBlank(version1)) {
            throw new RuntimeException("version is null");
        }
        return version0.equals(version1);
    }

    /**
     * @param version0 main version
     * @param version1 other version
     */
    public static boolean lowerThan(String version0, String version1) {
        if (StringUtils.isBlank(version0) || StringUtils.isBlank(version1)) {
            throw new RuntimeException("version is null");
        }
        version0 = version0.replace("-SNAPSHOT", "");
        version1 = version1.replace("-SNAPSHOT", "");
        String[] array0 = version0.split("\\.");
        String[] array1 = version1.split("\\.");
        if (array0.length != 3 || array1.length != 3) {
            throw new RuntimeException("version format is wrong");
        }
        int[] intArr0 = strArrayToInt(array0);
        int[] intArr1 = strArrayToInt(array1);
        boolean result = intArr0[0] < intArr1[0];
        if (result) {
            return true;
        }
        result = intArr0[0] == intArr1[0] && intArr0[1] < intArr1[1];
        if (result) {
            return true;
        }
        return intArr0[0] == intArr1[0] && intArr0[1] == intArr1[1] && intArr0[2] < intArr1[2];
    }

    private static int[] strArrayToInt(String[] array) {
        if (null == array || array.length == 0) {
            return null;
        }
        int[] intArr = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            String str = array[i];
            intArr[i] = Integer.parseInt(str);
        }
        return intArr;
    }
}
