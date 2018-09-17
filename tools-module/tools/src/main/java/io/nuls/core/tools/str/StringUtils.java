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
 */
public class StringUtils {

    /**
     * NULS
     */
    private static final String NULS = "NULS";

    public static final String EMPTY = "";

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
     *  Check the difficulty of the password
     *  length between 8 and 20, the combination of characters and numbers
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
        if (password.matches("(.*)[a-zA-z](.*)")
                && password.matches("(.*)\\d+(.*)")
                && !password.matches("(.*)\\s+(.*)")
                && !password.matches("(.*)[\u4e00-\u9fa5\u3000]+(.*)")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 别名规则:只允许使用小写字母、数字、下划线（下划线不能在两端）1~20字节
     * @param alias
     * @return
     */
    public static boolean validAlias(String alias) {
        try {
            if (isBlank(alias)) {
                return false;
            }
            alias = alias.trim();
            byte[] aliasBytes = alias.getBytes("UTF-8");
            if (aliasBytes.length < 1 || aliasBytes.length > 20) {
                return false;
            }
            if (alias.matches("^([a-z0-9]+[a-z0-9_]*[a-z0-9]+)|[a-z0-9]+${1,20}")) {
                return true;
            } else {
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    /**
     * token命名规则:只允许使用大、小写字母、数字、下划线（下划线不能在两端）1~20字节
     * @param name
     * @return
     */
    public static boolean validTokenNameOrSymbol(String name) {
        try {
            if (isBlank(name)) {
                return false;
            }

            String upperCaseName = name.toUpperCase();
            if(upperCaseName.contains(NULS)) {
                return false;
            }

            byte[] aliasBytes = name.getBytes("UTF-8");
            if (aliasBytes.length < 1 || aliasBytes.length > 20) {
                return false;
            }
            if (name.matches("^([a-zA-Z0-9]+[a-zA-Z0-9_]*[a-zA-Z0-9]+)|[a-zA-Z0-9]+${1,20}")) {
                return true;
            } else {
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    /**
     * 备注规则: 可以为空,或者不大于60字节
     * @param remark
     * @return
     */
    public static boolean validRemark(String remark) {
        try {
            if (null == remark) {
                return true;
            }
            remark = remark.trim();
            byte[] aliasBytes = remark.getBytes("UTF-8");
            if (aliasBytes.length < 0 || aliasBytes.length > 60) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
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

//    /**
//     * 验证是大于0的数(包含小数,不限位数)
//     * @param str
//     * @return
//     */
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

//    /**
//     * 去掉小数多余的.与0
//     * @param s
//     * @return
//     */
    private static String subZeroAndDot(String s){
        if(s.indexOf(".") > 0){
            s = s.replaceAll("0+?$", "");
            s = s.replaceAll("[.]$", "");
        }
        return s;
    }

    private static final Pattern NULS_PATTERN = Pattern.compile("([1-9]\\d*(\\.\\d{1,8})?)|(0\\.\\d{1,8})");

//    /**
//     * 匹配是否是nuls
//     * 验证是大于0的数(包括小数, 小数点后有效位超过8位则不合法)
//     * @param str
//     * @return
//     */
    public static boolean isNuls(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        str = subZeroAndDot(str);
        Matcher isNum = NULS_PATTERN.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private static final Pattern GT_ZERO_NUMBER_LIMIT_2_PATTERN = Pattern.compile("([1-9]\\d*(\\.\\d{1,2})?)|(0\\.\\d{1,2})");

//    /**
//     * 验证是大于0的数(包括小数, 小数点后有效位超过2位则不合法)
//     * @param str
//     * @return
//     */
    public static boolean isNumberGtZeroLimitTwo(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        str = subZeroAndDot(str);
        Matcher isNum = GT_ZERO_NUMBER_LIMIT_2_PATTERN.matcher(str);
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

    public static boolean validPubkeys(String pubkeys,String m){
        if(StringUtils.isBlank(pubkeys)){
            return  false;
        }
        if(m == null || Integer.parseInt(m) <= 0)
            return false;
        //将公钥拆分
        String[] dataList = pubkeys.split(",");
        if(dataList == null || dataList.length == 0 || dataList.length < Integer.parseInt(m)){
            return false;
        }
        return true;
    }
}
