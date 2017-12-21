package io.nuls.core.utils.str;

import io.nuls.core.constant.NulsConstant;
import io.nuls.core.context.NulsContext;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Niels on 2017/10/9.
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


    public static byte caculateXor(byte[] data) {
        byte xor = 0x00;
        if(data == null || data.length == 0) {
            return xor;
        }
        for (int i = 0; i < data.length; i++) {
            xor ^= data[i];
        }
        return xor;
    }
}
