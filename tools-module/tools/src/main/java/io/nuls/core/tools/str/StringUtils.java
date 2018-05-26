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
 
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Niels
 * @date 2017/10/9
 */
public class StringUtils {

    public static boolean isBlank(String str) {
        return null == str || str.trim().length() == 0;
    }

    public static boolean isNull(String str) {
        return null == str || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim());
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static boolean isNotNull(String str) {
        return !isNull(str);
    }

    public static String getNewUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String formatStringPara(String para) {
        return (isNull(para)) ? null : para.trim();
    }

    /**
     * Check the difficulty of the password
     * length between 8 and 20, the combination of characters and numbers
     *
     * @return boolean
     */
    public static boolean validPassword(String password) {
        if (isBlank(password)) {
            return false;
        }
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }
        if (password.matches("(.*)[a-zA-z](.*)") && password.matches("(.*)\\d+(.*)")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean validAlias(String alias) {
        try {
            if (isBlank(alias)) {
                return false;
            }
            alias = alias.trim();
            byte[] aliasBytes = alias.getBytes("UTF-8");
            if (aliasBytes.length < 3 || aliasBytes.length > 64) {
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        return true;
    }

    public static boolean validHash(String hash) {
        if (isBlank(hash)) {
            return false;
        }
        if (hash.length() > 73) {
            return false;
        }
        return true;
    }




    public static byte caculateXor(byte[] data) {
        byte xor = 0x00;
        if (data == null || data.length == 0) {
            return xor;
        }
        for (int i = 0; i < data.length; i++) {
            xor ^= data[i];
        }
        return xor;
    }

    public static boolean validAddressSimple(String address) {
        if (isBlank(address)) {
            return false;
        }
        if (address.length() > 40) {
            return false;
        }
        return true;
    }

    public static boolean isNumeric(String str) {
        for (int i = 0, len = str.length(); i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

    public static boolean isNumber(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        Matcher isNum = NUMBER_PATTERN.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private static final Pattern GT_ZERO_NUMBER_PATTERN = Pattern.compile("([1-9][0-9]*(\\.\\d+)?)|(0\\.\\d*[1-9]+0*)");

    public static boolean isNumberGtZero(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        Matcher isNum = GT_ZERO_NUMBER_PATTERN.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static byte[] bytes(String value) {
        return (value == null) ? null : value.getBytes(UTF_8);
    }

    public static String asString(byte[] value) {
        return (value == null) ? null : new String(value, UTF_8);
    }

    public static Long parseLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        String value = obj.toString();
        if (value.trim().length() == 0) {
            return 0L;
        }
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            return 0L;
        }
    }
}
