/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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

import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.ClassCodeLoader;
import io.nuls.contract.vm.code.FieldCode;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.util.Constants;
import io.nuls.contract.vm.util.Log;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MethodArea {

    public static final Map<String, ClassCode> INIT_CLASS_CODES = new HashMap<>(1024);

    public static final Map<String, MethodCode> INIT_METHOD_CODES = new HashMap<>(1024);

    private VM vm;

    private final Map<String, ClassCode> classCodes = new HashMap<>(1024);

    private final Map<String, MethodCode> methodCodes = new HashMap<>(1024);

    private final Map<String, ClassCode> loadClassCodes = new HashMap<>(1024);

    public MethodArea() {
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    public MethodCode loadMethod(String className, String methodName, String methodDesc) {
        String fullName;
        if (StringUtils.isNotEmpty(methodDesc)) {
            fullName = className + "." + methodName + methodDesc;
        } else {
            fullName = className + "." + methodName;
        }
        MethodCode methodCode = INIT_METHOD_CODES.get(fullName);
        if (methodCode != null) {
            return methodCode;
        }
        methodCode = methodCodes.get(fullName);
        if (methodCode != null) {
            return methodCode;
        }
        ClassCode classCode = loadClass(className);
        methodCode = classCode.getMethodCode(methodName, methodDesc);
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
        ClassCode classCode = INIT_CLASS_CODES.get(className);
        if (classCode != null) {
            return classCode;
        }
        classCode = this.classCodes.get(className);
        if (classCode != null) {
            return classCode;
        }
        classCode = ClassCodeLoader.getFromResource(className);
        if (classCode == null) {
            classCode = this.loadClassCodes.get(className);
        }
        if (classCode == null) {
            throw new RuntimeException("can't load class " + className);
        }
        this.classCodes.put(className, classCode);
        clinit(classCode);
        return classCode;
    }

    public void loadClassCodes(Map<String, ClassCode> classCodes) {
        if (classCodes != null) {
            this.loadClassCodes.putAll(classCodes);
            for (String className : classCodes.keySet()) {
                loadClass(className);
            }
        }
    }

    public Map<String, FieldCode> allFields(String className) {
        Map<String, FieldCode> fields = new LinkedHashMap<>();
        allFields(className, fields);
        return fields;
    }

    private void allFields(String className, Map<String, FieldCode> fields) {
        ClassCode classCode = loadClass(className);
        for (Map.Entry<String, FieldCode> entry : classCode.fields.entrySet()) {
            if (!fields.containsKey(entry.getKey())) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }
        if (classCode.superName != null) {
            allFields(classCode.superName, fields);
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
