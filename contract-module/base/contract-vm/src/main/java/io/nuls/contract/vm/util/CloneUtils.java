package io.nuls.contract.vm.util;

import io.nuls.contract.vm.ObjectRef;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;

public class CloneUtils {

    public static void clone(Map<String, Object> source, Map<String, Object> target) {
        for (String key : source.keySet()) {
            Object object = source.get(key);
            Object newObject = cloneObject(object);
            target.put(key, newObject);
        }
    }

    public static Map<String, Object> clone(Map<String, Object> source) {
        Map<String, Object> target = new LinkedHashMap<>();
        clone(source, target);
        return target;
    }

    public static Object cloneObject(Object object) {
        Object newObject = null;
        if (object == null) {
            newObject = null;
        } else if (object instanceof Integer) {
            newObject = ((Integer) object).intValue();
        } else if (object instanceof Long) {
            newObject = ((Long) object).longValue();
        } else if (object instanceof Float) {
            newObject = ((Float) object).floatValue();
        } else if (object instanceof Double) {
            newObject = ((Double) object).doubleValue();
        } else if (object instanceof Boolean) {
            newObject = ((Boolean) object).booleanValue();
        } else if (object instanceof Byte) {
            newObject = ((Byte) object).byteValue();
        } else if (object instanceof Character) {
            newObject = ((Character) object).charValue();
        } else if (object instanceof Short) {
            newObject = ((Short) object).shortValue();
        } else if (object instanceof String) {
            newObject = object;
        } else if (object instanceof ObjectRef) {
            newObject = object;
        } else if (object.getClass().isArray()) {
            int length = Array.getLength(object);
            Object array = Array.newInstance(object.getClass().getComponentType(), length);
            System.arraycopy(object, 0, array, 0, length);
            newObject = array;
        } else {
            newObject = object;
        }
        return newObject;
    }

}
