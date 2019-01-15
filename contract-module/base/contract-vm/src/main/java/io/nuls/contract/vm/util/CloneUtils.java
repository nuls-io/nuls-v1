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
package io.nuls.contract.vm.util;

import io.nuls.contract.vm.ObjectRef;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.nuls.contract.vm.util.Utils.hashMapInitialCapacity;

public class CloneUtils {

    public static void clone(Map<String, Object> source, Map<String, Object> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            Object newObject = cloneObject(object);
            target.put(key, newObject);
        }
    }

    public static Map<String, Object> clone(Map<String, Object> source) {
        Map<String, Object> target = new LinkedHashMap<>(hashMapInitialCapacity(source.size()));
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
