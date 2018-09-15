package io.nuls.contract.vm;

import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.ClassCodeLoader;
import io.nuls.contract.vm.code.FieldCode;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class MethodArea {

    public static final Map<String, ClassCode> INIT_CLASS_CODES = new HashMap<>(1024);

    public static final Map<String, MethodCode> INIT_METHOD_CODES = new HashMap<>(1024);

    private VM vm;

    private final Map<String, ClassCode> classCodes = new HashMap<>(1024);

    private final Map<String, MethodCode> methodCodes = new HashMap<>(1024);

    public MethodArea() {
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    public MethodCode loadMethod(String className, String methodName, String methodDesc) {
        final String fullName = className + "." + methodName + methodDesc;
        if (INIT_METHOD_CODES.containsKey(fullName)) {
            return INIT_METHOD_CODES.get(fullName);
        }
        if (methodCodes.containsKey(fullName)) {
            return methodCodes.get(fullName);
        }
        ClassCode classCode = loadClass(className);
        MethodCode methodCode = classCode.getMethodCode(methodName, methodDesc);
        if (methodCode == null && classCode.superName != null) {
            methodCode = loadSuperMethod(classCode.superName, methodName, methodDesc);
        }
        if (methodCode == null) {
            for (String interfaceName : classCode.interfaces) {
                methodCode = loadMethod(interfaceName, methodName, methodDesc);
                if (methodCode != null) {
                    break;
                }
            }
        }
        methodCodes.put(fullName, methodCode);
        return methodCode;
    }

    private MethodCode loadSuperMethod(String className, String methodName, String methodDesc) {
        ClassCode classCode = loadClass(className);
        MethodCode methodCode = classCode.getMethodCode(methodName, methodDesc);
        if (methodCode == null && classCode.superName != null) {
            methodCode = loadSuperMethod(classCode.superName, methodName, methodDesc);
        }
        return methodCode;
    }

    public ClassCode loadClass(String className) {
        if (INIT_CLASS_CODES.containsKey(className)) {
            return INIT_CLASS_CODES.get(className);
        } else {
            if (!this.classCodes.containsKey(className)) {
                ClassCode classCode = ClassCodeLoader.loadFromResource(className);
                loadClassCode(classCode);
            }
            return this.classCodes.get(className);
        }
    }

    public void loadClassCodes(Map<String, ClassCode> classCodes) {
        if (classCodes != null) {
            for (ClassCode classCode : classCodes.values()) {
                loadClassCode(classCode);
            }
        }
    }

    public void loadClassCode(ClassCode classCode) {
        String className = classCode.name;
        if (!INIT_CLASS_CODES.containsKey(className)) {
            if (!this.classCodes.containsKey(className)) {
                this.classCodes.put(className, classCode);
                //Log.loadClass(className);
                clinit(classCode);
            }
        }
    }

    private void clinit(ClassCode classCode) {
        for (FieldCode fieldCode : classCode.fields.values()) {
            if (fieldCode.isStatic && !fieldCode.isFinal) {
                this.vm.heap.putStatic(classCode.name, fieldCode.name, fieldCode.variableType.getDefaultValue());
            }
        }

        MethodCode methodCode = classCode.getMethodCode(Constants.CLINIT_NAME, Constants.CLINIT_DESC);
        if (methodCode != null) {
            this.vm.run(methodCode, null, true);
        }
    }

    public Map<String, ClassCode> getClassCodes() {
        return classCodes;
    }

    public Map<String, MethodCode> getMethodCodes() {
        return methodCodes;
    }

}
