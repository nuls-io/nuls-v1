package io.nuls.contract.vm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.nuls.contract.vm.ObjectRef;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void init() {
        Map<String, String> map = new LinkedHashMap<>();
        String json = toJson(map);
        toObject(json, Map.class);
        toArray("[1]", Integer.TYPE);
    }

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

    public static String encodeArray(Object value, Class<?> elementType) {
        String json;
        if (elementType == ObjectRef.class) {
            int length = Array.getLength(value);
            String[] array = new String[length];
            for (int i = 0; i < length; i++) {
                ObjectRef objectRef = (ObjectRef) Array.get(value, i);
                if (objectRef != null) {
                    array[i] = objectRef.getEncoded();
                }
            }
            json = toJson(array);
        } else {
            json = toJson(value);
        }
        return json;
    }

    public static Object decodeArray(byte[] bytes, Class<?> elementType) {
        String value = new String(bytes);
        if (elementType == ObjectRef.class) {
            Object array = toArray(value, String.class);
            int length = Array.getLength(array);
            ObjectRef[] objectRefs = new ObjectRef[length];
            for (int i = 0; i < length; i++) {
                String s = (String) Array.get(array, i);
                if (s != null) {
                    objectRefs[i] = new ObjectRef(s);
                }
            }
            return objectRefs;
        } else {
            return toArray(value, elementType);
        }
    }

    public static String encode(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Map) {
            Map map = (Map) value;
            Map map1 = new LinkedHashMap();
            map.forEach((k, v) -> {
                map1.put(k, encode(v));
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
            return "__" + value;
        } else if (value instanceof ObjectRef) {
            return "R_" + ((ObjectRef) value).getEncoded();
        } else {
            throw new IllegalArgumentException("unknown value");
        }
    }

    public static Object decode(String str) {
        if (str == null) {
            return null;
        }
        String prefix = str.substring(0, 1);
        String value = str.substring(2);
        switch (prefix) {
            case "{":
                Map<String, String> map = toObject(str, Map.class);
                Map<String, Object> objectMap = new LinkedHashMap<>();
                map.forEach((k, v) -> {
                    objectMap.put(k, decode(v));
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
            case "_":
                return value;
            case "R":
                return new ObjectRef(value);
            default:
                throw new IllegalArgumentException("unknown string");
        }
    }

    public static byte[] compress(String data) {
//        try {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
//            GZIPOutputStream gzip = new GZIPOutputStream(bos);
//            gzip.write(data.getBytes());
//            gzip.close();
//            byte[] compressed = bos.toByteArray();
//            bos.close();
//            return compressed;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return data.getBytes();
    }

    public static String decompress(byte[] compressed) {
//        try {
//            ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
//            GZIPInputStream gis = new GZIPInputStream(bis);
//            byte[] bytes = IOUtils.toByteArray(gis);
//            return new String(bytes);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        return new String(compressed);
    }

}
