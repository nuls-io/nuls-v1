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
package io.nuls.contract.vm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.BiMap;
import io.nuls.contract.vm.code.VariableType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ObjectRef {

    public static final Map<String, Integer> map = new HashMap<>();

    private final String ref;

    private final String desc;

    private final int[] dimensions;

    @JsonIgnore
    private final VariableType variableType;

    public ObjectRef(String ref, String desc, int... dimensions) {
        this.ref = ref;
        this.desc = desc;
        this.dimensions = dimensions;
        this.variableType = VariableType.valueOf(this.desc);
    }

    public ObjectRef(String str, BiMap<String, String> classNames) {
        String[] parts = str.split(",");
        int[] dimensions = new int[parts.length - 2];
        for (int i = 0; i < dimensions.length; i++) {
            int dimension = Integer.valueOf(parts[i + 2]);
            dimensions[i] = dimension;
        }
        this.ref = parts[0];
        String s = parts[1];
        String s1 = classNames.get(s);
        if (s1 != null) {
            s = s1;
        }
        this.desc = s;
        this.dimensions = dimensions;
        this.variableType = VariableType.valueOf(this.desc);
    }

    public String getEncoded(BiMap<String, String> classNames) {
        StringBuilder sb = new StringBuilder();
//        Integer i = map.get(desc);
//        if (i == null) {
//            i = 0;
//        }
//        map.put(desc, i + 1);
        String s = desc;
        String s1 = classNames.inverse().get(s);
        if (s1 != null) {
            s = s1;
        }
        sb.append(ref).append(",").append(s);
        for (int dimension : dimensions) {
            sb.append(",").append(dimension);
        }
        return sb.toString();
    }

    public boolean isArray() {
        return this.dimensions != null && this.dimensions.length > 0;
    }

    public String getRef() {
        return ref;
    }

    public String getDesc() {
        return desc;
    }

    public int[] getDimensions() {
        return dimensions;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ObjectRef objectRef = (ObjectRef) o;

        if (ref != null ? !ref.equals(objectRef.ref) : objectRef.ref != null) {
            return false;
        }
        if (desc != null ? !desc.equals(objectRef.desc) : objectRef.desc != null) {
            return false;
        }
        return Arrays.equals(dimensions, objectRef.dimensions);
    }

    @Override
    public int hashCode() {
        int result = ref != null ? ref.hashCode() : 0;
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(dimensions);
        return result;
    }

    @Override
    public String toString() {
        return "ObjectRef{" +
                "ref=" + ref +
                ", desc=" + desc +
                ", dimensions=" + Arrays.toString(dimensions) +
                '}';
    }

}
