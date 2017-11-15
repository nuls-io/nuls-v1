package io.nuls.core.utils.str;

import java.util.UUID;

/**
 * Created by Niels on 2017/10/9.
 *
 */
public class StringUtils {

    public static boolean isBlank(String str) {
        return null == str || str.trim().length() == 0;
    }

    public static boolean isNull(String str) {
        return null == str || str.trim().length() == 0 || str.trim().equalsIgnoreCase("null");
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    public static boolean isNotNull(String str) {
        return !isNull (str);
    }

    public static String getNewUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
