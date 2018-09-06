package io.nuls.contract.vm.code;

import java.util.*;

public class ClassCodes {

    private Map<String, ClassCode> classCodeMap;

    public ClassCodes(Map<String, ClassCode> classCodeMap) {
        this.classCodeMap = classCodeMap;
    }

    public ClassCodes(Collection<ClassCode> classCodes) {
        Map<String, ClassCode> classCodeMap = new LinkedHashMap<>();
        for (ClassCode classCode : classCodes) {
            classCodeMap.put(classCode.getName(), classCode);
        }
        this.classCodeMap = classCodeMap;
    }

    public boolean instanceOf(final ClassCode classCode, final String interfaceName) {
        if (classCode.getInterfaces().contains(interfaceName)) {
            return true;
        } else {
            if (classCode.getSuperName() != null && classCodeMap.containsKey(classCode.getSuperName())) {
                return instanceOf(classCodeMap.get(classCode.getSuperName()), interfaceName);
            } else {
                return false;
            }
        }
    }

    public Set<ClassCode> allClasses(final ClassCode classCode) {
        Set<ClassCode> set = new LinkedHashSet<>();
        allClasses(classCode, set);
        return set;
    }

    private void allClasses(final ClassCode classCode, Set<ClassCode> set) {
        if (set.contains(classCode)) {
            return;
        }
        set.add(classCode);
        if (classCodeMap.containsKey(classCode.getSuperName())) {
            allClasses(classCodeMap.get(classCode.getSuperName()), set);
        }
        classCode.getInterfaces().forEach(interfaceName -> {
            if (classCodeMap.containsKey(interfaceName)) {
                allClasses(classCodeMap.get(interfaceName), set);
            }
        });
        classCode.getInnerClasses().forEach(innerClassNode -> {
            if (classCodeMap.containsKey(innerClassNode.name)) {
                allClasses(classCodeMap.get(innerClassNode.name), set);
            }
        });
    }

}
