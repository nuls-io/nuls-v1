package io.nuls.rpc.sdk.utils;


import java.util.List;
import java.util.Map;

/**
 * Created by Niels on 2017/9/29.
 *
 */
public final class AssertUtil {

    public static void isEquals(Object val1, Object val2, String msg) {
        if (val1 == val2 || (val1 != null && val1.equals(val2))) {
            return;
        }
        throw new RuntimeException(msg);
    }

    public static void canNotEmpty(Object val, String msg) {
        boolean b = false;
        do {
            if (null == val) {
                b = true;
                break;
            }
            if (val instanceof String) {
                b = null == val || ((String) val).trim().length() == 0;
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
