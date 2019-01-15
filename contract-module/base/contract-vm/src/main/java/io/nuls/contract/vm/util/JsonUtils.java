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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.BiMap;
import io.nuls.contract.vm.ObjectRef;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.nuls.contract.vm.util.Utils.hashMapInitialCapacity;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toObject(String value, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(value, valueType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toArray(String value, Class<?> elementType) {
        ArrayType arrayType = TypeFactory.defaultInstance().constructArrayType(elementType);
        try {
            return OBJECT_MAPPER.readValue(value, arrayType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeArray(Object value, Class<?> elementType, BiMap<String, String> classNames) {
        String json;
        if (elementType == ObjectRef.class) {
            int length = Array.getLength(value);
            String[] array = new String[length];
            for (int i = 0; i < length; i++) {
                ObjectRef objectRef = (ObjectRef) Array.get(value, i);
                if (objectRef != null) {
                    array[i] = objectRef.getEncoded(classNames);
                }
            }
            json = toJson(array);
        } else {
            json = toJson(value);
        }
        return json;
    }

    public static Object decodeArray(String value, Class<?> elementType, BiMap<String, String> classNames) {
        if (elementType == ObjectRef.class) {
            Object array = toArray(value, String.class);
            int length = Array.getLength(array);
            ObjectRef[] objectRefs = new ObjectRef[length];
            for (int i = 0; i < length; i++) {
                String s = (String) Array.get(array, i);
                if (s != null) {
                    objectRefs[i] = new ObjectRef(s, classNames);
                }
            }
            return objectRefs;
        } else {
            return toArray(value, elementType);
        }
    }

    public static String encode(Object value, BiMap<String, String> classNames) {
        if (value == null) {
            return null;
        } else if (value.getClass().isArray()) {
            Class clazz = value.getClass().getComponentType();
            if (clazz == Integer.TYPE) {
                return "[I_" + encodeArray(value, clazz, classNames);
            } else if (clazz == Long.TYPE) {
                return "[J_" + encodeArray(value, clazz, classNames);
            } else if (clazz == Float.TYPE) {
                return "[F_" + encodeArray(value, clazz, classNames);
            } else if (clazz == Double.TYPE) {
                return "[D_" + encodeArray(value, clazz, classNames);
            } else if (clazz == Boolean.TYPE) {
                return "[Z_" + encodeArray(value, clazz, classNames);
            } else if (clazz == Byte.TYPE) {
                return "[B_" + encodeArray(value, clazz, classNames);
            } else if (clazz == Character.TYPE) {
                return "[C_" + encodeArray(value, clazz, classNames);
            } else if (clazz == Short.TYPE) {
                return "[S_" + encodeArray(value, clazz, classNames);
            } else {
                return "[R_" + encodeArray(value, clazz, classNames);
            }
        } else if (value instanceof Map) {
            Map map = (Map) value;
            Map map1 = new LinkedHashMap(hashMapInitialCapacity(map.size()));
            map.forEach((k, v) -> {
                map1.put(k, encode(v, classNames));
            });
            return toJson(map1);
        } else if (value instanceof Integer) {
            return "I_" + value;
        } else if (value instanceof Long) {
            return "J_" + value;
        } else if (value instanceof Float) {
            return "F_" + value;
        } else if (value instanceof Double) {
            return "D_" + value;
        } else if (value instanceof Boolean) {
            return "Z_" + value;
        } else if (value instanceof Byte) {
            return "B_" + value;
        } else if (value instanceof Character) {
            return "C_" + value;
        } else if (value instanceof Short) {
            return "S_" + value;
        } else if (value instanceof String) {
            return "s_" + value;
        } else if (value instanceof ObjectRef) {
            return "R_" + ((ObjectRef) value).getEncoded(classNames);
        } else {
            throw new IllegalArgumentException("unknown value");
        }
    }

    public static Object decode(String str, BiMap<String, String> classNames) {
        if (str == null) {
            return null;
        }
        String prefix = str.substring(0, 1);
        String value = str.substring(2);
        if (!"{".equals(prefix)) {
            String[] parts = str.split("_", 2);
            prefix = parts[0];
            value = parts[1];
        }
        switch (prefix) {
            case "{":
                Map<String, String> map = toObject(str, Map.class);
                Map<String, Object> objectMap = new LinkedHashMap<>(hashMapInitialCapacity(map.size()));
                map.forEach((k, v) -> {
                    objectMap.put(k, decode(v, classNames));
                });
                return objectMap;
            case "I":
                return Integer.valueOf(value).intValue();
            case "J":
                return Long.valueOf(value).longValue();
            case "F":
                return Float.valueOf(value).floatValue();
            case "D":
                return Double.valueOf(value).doubleValue();
            case "Z":
                return Boolean.valueOf(value).booleanValue();
            case "B":
                return Byte.valueOf(value).byteValue();
            case "C":
                return value.charAt(0);
            case "S":
                return Short.valueOf(value).shortValue();
            case "s":
                return value;
            case "R":
                return new ObjectRef(value, classNames);
            case "[I":
                return decodeArray(value, Integer.TYPE, classNames);
            case "[J":
                return decodeArray(value, Long.TYPE, classNames);
            case "[F":
                return decodeArray(value, Float.TYPE, classNames);
            case "[D":
                return decodeArray(value, Double.TYPE, classNames);
            case "[Z":
                return decodeArray(value, Boolean.TYPE, classNames);
            case "[B":
                return decodeArray(value, Byte.TYPE, classNames);
            case "[C":
                return decodeArray(value, Character.TYPE, classNames);
            case "[S":
                return decodeArray(value, Short.TYPE, classNames);
            case "[R":
                return decodeArray(value, ObjectRef.class, classNames);
            default:
                throw new IllegalArgumentException("unknown string");
        }
    }

}
