/**
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
 */
package io.nuls.core.utils.str;

import io.nuls.core.context.NulsContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.utils.crypto.Base58;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 *
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

    /**
     * Check the difficulty of the password
     * length between 8 and 20, the combination of characters and numbers
     *
     * @param password
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
            byte[] aliasBytes = alias.getBytes(NulsContext.DEFAULT_ENCODING);
            if (aliasBytes.length < 3 || aliasBytes.length > 20) {
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            return false;
        }
        return true;
    }

    public static boolean validHash(String hash) {
        if (isBlank(hash)){
            return false;
        }
        if (hash.length() > 73){
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
}
