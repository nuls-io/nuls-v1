package io.nuls.contract.vm.code;

import java.util.Map;

public class ClassCodes {

    private final Map<String, ClassCode> classCodeMap;

    public ClassCodes(Map<String, ClassCode> classCodeMap) {
        this.classCodeMap = classCodeMap;
    }

    public boolean instanceOf(final ClassCode classCode, final String interfaceName) {
        if (classCode.interfaces.contains(interfaceName)) {
            return true;
        } else {
            if (classCode.superName != null && classCodeMap.containsKey(classCode.superName)) {
                return instanceOf(classCodeMap.get(classCode.superName), interfaceName);
            } else {
                return false;
            }
        }
    }

}
