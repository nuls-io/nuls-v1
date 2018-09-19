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
        if (Descriptors.DESCRIPTORS.inverse().containsKey(desc)) {
            desc = Descriptors.DESCRIPTORS.inverse().get(desc);
        } else {
            if (ProgramDescriptors.DESCRIPTORS.inverse().containsKey(desc)) {
                desc = ProgramDescriptors.DESCRIPTORS.inverse().get(desc);
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
        if (Descriptors.DESCRIPTORS.containsKey(desc)) {
            desc = Descriptors.DESCRIPTORS.get(desc);
        } else {
            if (ProgramDescriptors.DESCRIPTORS.containsKey(desc)) {
                desc = ProgramDescriptors.DESCRIPTORS.get(desc);
            }
        }
        for (int i = 0; i < dimensions; i++) {
            desc = "[" + desc;
        }
        return desc;
    }

}
