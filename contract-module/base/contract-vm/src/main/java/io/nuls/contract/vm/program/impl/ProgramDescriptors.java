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
package io.nuls.contract.vm.program.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.nuls.contract.vm.code.Descriptors;
import io.nuls.contract.vm.code.VariableType;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProgramDescriptors {

    public static final BiMap<String, String> DESCRIPTORS;

    static {
        DESCRIPTORS = HashBiMap.create();
        DESCRIPTORS.put("Boolean", "Ljava/lang/Boolean;");
        DESCRIPTORS.put("Byte", "Ljava/lang/Byte;");
        DESCRIPTORS.put("Short", "Ljava/lang/Short;");
        DESCRIPTORS.put("Character", "Ljava/lang/Character;");
        DESCRIPTORS.put("Integer", "Ljava/lang/Integer;");
        DESCRIPTORS.put("Long", "Ljava/lang/Long;");
        DESCRIPTORS.put("Float", "Ljava/lang/Float;");
        DESCRIPTORS.put("Double", "Ljava/lang/Double;");
        DESCRIPTORS.put("String", "Ljava/lang/String;");
        DESCRIPTORS.put("BigInteger", "Ljava/math/BigInteger;");
        DESCRIPTORS.put("Address", "Lio/nuls/contract/sdk/Address;");
    }

    private static final Pattern PATTERN = Pattern.compile("^\\((.*)\\) return (.+)$");

    public static String getNormalDesc(VariableType variableType) {
        String desc = variableType.getDesc().replace("[", "");
        String desc1 = Descriptors.DESCRIPTORS.inverse().get(desc);
        if (desc1 != null) {
            desc = desc1;
        } else {
            desc1 = ProgramDescriptors.DESCRIPTORS.inverse().get(desc);
            if (desc1 != null) {
                desc = desc1;
            }
        }
        if (variableType.isArray()) {
            for (int i = 0; i < variableType.getDimensions(); i++) {
                desc += "[]";
            }
        }
        return desc;
    }

    public static String parseDesc(String desc) {
        if (desc == null) {
            return null;
        }

        desc = desc.trim();
        StringBuilder sb = new StringBuilder();

        Matcher matcher = PATTERN.matcher(desc);
        if (matcher.matches()) {
            sb.append("(");
            String arg = matcher.group(1);
            if (StringUtils.isNotEmpty(arg)) {
                String[] args = arg.split(", ");
                for (String s : args) {
                    sb.append(getDesc(s));
                }
            }
            sb.append(")");
            String returnArg = matcher.group(2);
            sb.append(getDesc(returnArg));
        } else {
            sb.append(desc);
        }

        return sb.toString();
    }

    private static String getDesc(String desc) {
        int dimensions = StringUtils.countMatches(desc, "[]");
        desc = desc.replace("[]", "");
        String[] parts = desc.split(" ");
        desc = parts[0];
        String desc1 = Descriptors.DESCRIPTORS.get(desc);
        if (desc1 != null) {
            desc = desc1;
        } else {
            desc1 = ProgramDescriptors.DESCRIPTORS.get(desc);
            if (desc1 != null) {
                desc = desc1;
            }
        }
        for (int i = 0; i < dimensions; i++) {
            desc = "[" + desc;
        }
        return desc;
    }

}
