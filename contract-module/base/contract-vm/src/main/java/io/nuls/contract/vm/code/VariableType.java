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
package io.nuls.contract.vm.code;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static io.nuls.contract.vm.util.Utils.arrayListInitialCapacity;

public class VariableType {

    private static final LoadingCache<String, VariableType> CACHE;
    private static final LoadingCache<String, List<VariableType>> CACHE_LIST;

    static {
        CACHE = CacheBuilder.newBuilder()
                .initialCapacity(1024)
                .maximumSize(102400)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<String, VariableType>() {
                    @Override
                    public VariableType load(String desc) {
                        return new VariableType(desc);
                    }
                });
        CACHE_LIST = CacheBuilder.newBuilder()
                .initialCapacity(1024)
                .maximumSize(10240)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<String, List<VariableType>>() {
                    @Override
                    public List<VariableType> load(String desc) {
                        return parseList(desc);
                    }
                });
    }

    public static final VariableType INT_TYPE = valueOf("I");
    public static final VariableType LONG_TYPE = valueOf("J");
    public static final VariableType FLOAT_TYPE = valueOf("F");
    public static final VariableType DOUBLE_TYPE = valueOf("D");
    public static final VariableType BOOLEAN_TYPE = valueOf("Z");
    public static final VariableType BYTE_TYPE = valueOf("B");
    public static final VariableType CHAR_TYPE = valueOf("C");
    public static final VariableType SHORT_TYPE = valueOf("S");
    public static final VariableType INT_WRAPPER_TYPE = valueOf("Ljava/lang/Integer;");
    public static final VariableType LONG_WRAPPER_TYPE = valueOf("Ljava/lang/Long;");
    public static final VariableType FLOAT_WRAPPER_TYPE = valueOf("Ljava/lang/Float;");
    public static final VariableType DOUBLE_WRAPPER_TYPE = valueOf("Ljava/lang/Double;");
    public static final VariableType BOOLEAN_WRAPPER_TYPE = valueOf("Ljava/lang/Boolean;");
    public static final VariableType BYTE_WRAPPER_TYPE = valueOf("Ljava/lang/Byte;");
    public static final VariableType CHAR_WRAPPER_TYPE = valueOf("Ljava/lang/Character;");
    public static final VariableType SHORT_WRAPPER_TYPE = valueOf("Ljava/lang/Short;");
    public static final VariableType OBJECT_TYPE = valueOf("Ljava/lang/Object;");
    public static final VariableType STRING_TYPE = valueOf("Ljava/lang/String;");
    public static final VariableType RUNTIME_EXCEPTION_TYPE = valueOf("Ljava/lang/RuntimeException;");
    public static final VariableType NUMBER_FORMAT_EXCEPTION_TYPE = valueOf("Ljava/lang/NumberFormatException;");
    public static final VariableType NULL_POINTER_EXCEPTION_TYPE = valueOf("Ljava/lang/NullPointerException;");
    public static final VariableType ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_TYPE = valueOf("Ljava/lang/ArrayIndexOutOfBoundsException;");
    public static final VariableType NEGATIVE_ARRAY_SIZE_EXCEPTION_TYPE = valueOf("Ljava/lang/NegativeArraySizeException;");
    public static final VariableType CLASS_CAST_EXCEPTION_TYPE = valueOf("Ljava/lang/ClassCastException;");
    public static final VariableType STACK_OVERFLOW_ERROR_TYPE = valueOf("Ljava/lang/StackOverflowError;");
    public static final VariableType BIGINTEGER_TYPE = valueOf("Ljava/math/BigInteger;");
    public static final VariableType STRINGBUILDER_TYPE = valueOf("Ljava/lang/StringBuilder;");
    public static final VariableType ADDRESS_TYPE = valueOf("Lio/nuls/contract/sdk/Address;");
    public static final VariableType BLOCK_HEADER_TYPE = valueOf("Lio/nuls/contract/sdk/BlockHeader;");
    public static final VariableType INT_ARRAY_TYPE = valueOf("[I");
    public static final VariableType LONG_ARRAY_TYPE = valueOf("[J");
    public static final VariableType FLOAT_ARRAY_TYPE = valueOf("[F");
    public static final VariableType DOUBLE_ARRAY_TYPE = valueOf("[D");
    public static final VariableType BOOLEAN_ARRAY_TYPE = valueOf("[Z");
    public static final VariableType BYTE_ARRAY_TYPE = valueOf("[B");
    public static final VariableType CHAR_ARRAY_TYPE = valueOf("[C");
    public static final VariableType SHORT_ARRAY_TYPE = valueOf("[S");
    public static final VariableType STRING_ARRAY_TYPE = valueOf("[Ljava/lang/String;");
    public static final VariableType STACK_TRACE_ELEMENT_TYPE = valueOf("Ljava/lang/StackTraceElement;");
    public static final VariableType STACK_TRACE_ELEMENT_ARRAY_TYPE = valueOf("[Ljava/lang/StackTraceElement;");

    public static final VariableType[] WRAPPER_TYPE = new VariableType[]{
            INT_WRAPPER_TYPE,
            LONG_WRAPPER_TYPE,
            FLOAT_WRAPPER_TYPE,
            DOUBLE_WRAPPER_TYPE,
            BOOLEAN_WRAPPER_TYPE,
            BYTE_WRAPPER_TYPE,
            CHAR_WRAPPER_TYPE,
            SHORT_WRAPPER_TYPE
    };

    public static final BiMap<String, String> DESCRIPTORS;

    static {
        DESCRIPTORS = HashBiMap.create();
        DESCRIPTORS.put("z", "Ljava/lang/Boolean;");
        DESCRIPTORS.put("[z", "[Ljava/lang/Boolean;");
        DESCRIPTORS.put("b", "Ljava/lang/Byte;");
        DESCRIPTORS.put("[b", "[Ljava/lang/Byte;");
        DESCRIPTORS.put("s", "Ljava/lang/Short;");
        DESCRIPTORS.put("[s", "[Ljava/lang/Short;");
        DESCRIPTORS.put("c", "Ljava/lang/Character;");
        DESCRIPTORS.put("[c", "[Ljava/lang/Character;");
        DESCRIPTORS.put("i", "Ljava/lang/Integer;");
        DESCRIPTORS.put("[i", "[Ljava/lang/Integer;");
        DESCRIPTORS.put("l", "Ljava/lang/Long;");
        DESCRIPTORS.put("[l", "[Ljava/lang/Long;");
        DESCRIPTORS.put("f", "Ljava/lang/Float;");
        DESCRIPTORS.put("[f", "[Ljava/lang/Float;");
        DESCRIPTORS.put("d", "Ljava/lang/Double;");
        DESCRIPTORS.put("[d", "[Ljava/lang/Double;");
        DESCRIPTORS.put("r", "Ljava/lang/String;");
        DESCRIPTORS.put("[r", "[Ljava/lang/String;");
        DESCRIPTORS.put("e", "Ljava/math/BigInteger;");
        DESCRIPTORS.put("[e", "[Ljava/math/BigInteger;");
        DESCRIPTORS.put("a", "Lio/nuls/contract/sdk/Address;");
        DESCRIPTORS.put("[a", "[Lio/nuls/contract/sdk/Address;");
        DESCRIPTORS.put("m", "Ljava/util/HashMap;");
        DESCRIPTORS.put("n", "Ljava/util/HashMap$Node;");
        DESCRIPTORS.put("[n", "[Ljava/util/HashMap$Node;");
        DESCRIPTORS.put("g", "Ljava/util/ArrayList;");
        DESCRIPTORS.put("o", "Ljava/lang/Object;");
        DESCRIPTORS.put("[o", "[Ljava/lang/Object;");
    }

    private String desc;

    private String type;

    private VariableType componentType;

    private boolean primitiveType;

    private boolean primitive;

    private boolean array;

    private int dimensions;

    private Object defaultValue;

    private VariableType(String desc) {
        final String descriptor = Descriptors.DESCRIPTORS.get(desc);
        if (descriptor != null) {
            this.desc = descriptor;
        } else {
            this.desc = desc;
        }
        this.type = this.desc;
        if (this.type.contains("[")) {
            this.array = true;
            this.dimensions = this.type.lastIndexOf("[") + 1;
            this.type = this.desc.replace("[", "");
            this.componentType = valueOf(this.desc.replaceFirst("\\[", ""));
        }
        final String type = Descriptors.DESCRIPTORS.inverse().get(this.type);
        if (type != null) {
            this.type = type;
            this.primitiveType = isNotVoid();
        } else if (this.type.startsWith("L") && this.type.endsWith(";")) {
            this.type = this.type.substring(1, this.type.length() - 1);
        } else {
            this.desc = "L" + this.type + ";";
        }
        if (!this.array && this.primitiveType) {
            this.primitive = true;
            this.defaultValue = this.defaultValue();
        }
    }

    public static VariableType valueOf(String desc) {
        try {
            VariableType variableType = CACHE.get(desc);
            if (!variableType.getDesc().equals(desc)) {
                variableType = CACHE.get(variableType.getDesc());
                CACHE.put(desc, variableType);
            }
            return variableType;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<VariableType> parseArgs(String desc) {
        List<VariableType> args = parseAll(desc);
        return args.subList(0, args.size() - 1);
    }

    public static List<VariableType> parseAll(String desc) {
        try {
            return CACHE_LIST.get(desc);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<VariableType> parseList(String desc) {
        List<String> list = Descriptors.parse(desc, true);
        List<VariableType> args = new ArrayList<>(arrayListInitialCapacity(list.size()));
        for (String type : list) {
            args.add(valueOf(type));
        }
        return args;
    }

    public boolean isVoid() {
        return Descriptors.VOID.equals(this.type);
    }

    public boolean isNotVoid() {
        return !isVoid();
    }

    public boolean isByte() {
        return primitive && Descriptors.BYTE.equals(this.type);
    }

    public boolean isChar() {
        return primitive && Descriptors.CHAR.equals(this.type);
    }

    public boolean isDouble() {
        return primitive && Descriptors.DOUBLE.equals(this.type);
    }

    public boolean isFloat() {
        return primitive && Descriptors.FLOAT.equals(this.type);
    }

    public boolean isInt() {
        return primitive && Descriptors.INT.equals(this.type);
    }

    public boolean isLong() {
        return primitive && Descriptors.LONG.equals(this.type);
    }

    public boolean isShort() {
        return primitive && Descriptors.SHORT.equals(this.type);
    }

    public boolean isBoolean() {
        return primitive && Descriptors.BOOLEAN.equals(this.type);
    }

    public Object defaultValue() {
        Object defaultValue = null;
        switch (this.type) {
            case Descriptors.INT:
                defaultValue = 0;
                break;
            case Descriptors.LONG:
                defaultValue = 0L;
                break;
            case Descriptors.FLOAT:
                defaultValue = 0.0F;
                break;
            case Descriptors.DOUBLE:
                defaultValue = 0.0D;
                break;
            case Descriptors.BOOLEAN:
                defaultValue = false;
                break;
            case Descriptors.BYTE:
                defaultValue = (byte) 0;
                break;
            case Descriptors.CHAR:
                defaultValue = '\u0000';
                break;
            case Descriptors.SHORT:
                defaultValue = (short) 0;
                break;
            default:
                break;
        }
        return defaultValue;
    }

    public Class getPrimitiveTypeClass() {
        Class clazz = null;
        if (!this.primitiveType) {
            return clazz;
        }
        switch (this.type) {
            case Descriptors.INT:
                clazz = Integer.TYPE;
                break;
            case Descriptors.LONG:
                clazz = Long.TYPE;
                break;
            case Descriptors.FLOAT:
                clazz = Float.TYPE;
                break;
            case Descriptors.DOUBLE:
                clazz = Double.TYPE;
                break;
            case Descriptors.BOOLEAN:
                clazz = Boolean.TYPE;
                break;
            case Descriptors.BYTE:
                clazz = Byte.TYPE;
                break;
            case Descriptors.CHAR:
                clazz = Character.TYPE;
                break;
            case Descriptors.SHORT:
                clazz = Short.TYPE;
                break;
            default:
                break;
        }
        return clazz;
    }

    public Object getPrimitiveValue(Object value) {
        if (this.primitive) {
            if (value == null || "".equals(value.toString())) {
                value = defaultValue();
            } else {
                String s = value.toString();
                switch (this.type) {
                    case Descriptors.INT:
                        value = Integer.valueOf(s).intValue();
                        break;
                    case Descriptors.LONG:
                        value = Long.valueOf(s).longValue();
                        break;
                    case Descriptors.FLOAT:
                        value = Float.valueOf(s).floatValue();
                        break;
                    case Descriptors.DOUBLE:
                        value = Double.valueOf(s).doubleValue();
                        break;
                    case Descriptors.BOOLEAN:
                        if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
                            value = true;
                        } else {
                            value = false;
                        }
                        break;
                    case Descriptors.BYTE:
                        value = Byte.valueOf(s).byteValue();
                        break;
                    case Descriptors.CHAR:
                        if (value instanceof Integer) {
                            value = (char) ((Integer) value).intValue();
                        } else {
                            value = s.charAt(0);
                        }
                        break;
                    case Descriptors.SHORT:
                        value = Short.valueOf(s).shortValue();
                        break;
                    default:
                        break;
                }
            }
        }
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    public VariableType getComponentType() {
        return componentType;
    }

    public boolean isPrimitiveType() {
        return primitiveType;
    }

    public boolean isWrapperType() {
        return ArrayUtils.contains(WRAPPER_TYPE, this);
    }

    public boolean isStringType() {
        return STRING_TYPE.equals(this);
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public boolean isArray() {
        return array;
    }

    public int getDimensions() {
        return dimensions;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VariableType that = (VariableType) o;

        if (primitiveType != that.primitiveType) {
            return false;
        }
        if (primitive != that.primitive) {
            return false;
        }
        if (array != that.array) {
            return false;
        }
        if (dimensions != that.dimensions) {
            return false;
        }
        if (desc != null ? !desc.equals(that.desc) : that.desc != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (componentType != null ? !componentType.equals(that.componentType) : that.componentType != null) {
            return false;
        }
        return defaultValue != null ? defaultValue.equals(that.defaultValue) : that.defaultValue == null;
    }

    @Override
    public int hashCode() {
        int result = desc != null ? desc.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (componentType != null ? componentType.hashCode() : 0);
        result = 31 * result + (primitiveType ? 1 : 0);
        result = 31 * result + (primitive ? 1 : 0);
        result = 31 * result + (array ? 1 : 0);
        result = 31 * result + dimensions;
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VariableType{" +
                "desc=" + desc +
                ", type=" + type +
                ", componentType=" + componentType +
                ", primitiveType=" + primitiveType +
                ", primitive=" + primitive +
                ", array=" + array +
                ", dimensions=" + dimensions +
                ", defaultValue=" + defaultValue +
                '}';
    }

}
