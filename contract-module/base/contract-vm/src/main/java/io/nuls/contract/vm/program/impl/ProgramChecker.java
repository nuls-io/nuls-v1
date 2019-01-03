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

import com.google.common.base.Joiner;
import io.nuls.contract.vm.OpCode;
import io.nuls.contract.vm.code.*;
import org.apache.commons.collections4.CollectionUtils;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramChecker {

    private static final Logger log = LoggerFactory.getLogger(ProgramExecutorImpl.class);

    public static void check(Map<String, ClassCode> classCodes) {
        checkJdkVersion(classCodes);
        checkContractNum(classCodes);
        //checkStaticField(classCodes);
        checkClass(classCodes);
        checkContractMethodArgs(classCodes);
        checkMethod(classCodes);
        checkOpCode(classCodes);
    }

    public static void checkJdkVersion(Map<String, ClassCode> classCodes) {
        for (ClassCode classCode : classCodes.values()) {
            if (!classCode.isV1_6 && !classCode.isV1_8) {
                throw new RuntimeException("class version must be 1.6 or 1.8");
            }
        }
    }

    public static void checkContractNum(Map<String, ClassCode> classCodes) {
        List<ClassCode> contractClassCodes = classCodes.values().stream()
                .filter(classCode -> classCode.interfaces.contains(ProgramConstants.CONTRACT_INTERFACE_NAME))
                .collect(Collectors.toList());
        int contractCount = contractClassCodes.size();
        if (contractCount != 1) {
            throw new RuntimeException(String.format("find %s contracts", contractCount));
        }
    }

    public static void checkContractMethodArgs(Map<String, ClassCode> classCodes) {
        final List<MethodCode> list = ProgramExecutorImpl.getProgramMethodCodes(classCodes);
        for (MethodCode methodCode : list) {
            final List<VariableType> variableTypes = methodCode.argsVariableType;
            for (VariableType variableType : variableTypes) {
                if (variableType.isPrimitive()) {
                    //
                } else if (variableType.isArray()) {
                    if (variableType.getDimensions() > 1) {
                        throw new RuntimeException(String.format("only one-dimensional array can be used in method %s.%s", methodCode.className, methodCode.name));
                    } else if (!(variableType.isPrimitiveType() || variableType.getComponentType().isStringType() || variableType.getComponentType().isWrapperType())) {
                        throw new RuntimeException(String.format("only primitive type array and string array can be used in method %s.%s", methodCode.className, methodCode.name));
                    } else {
                        //
                    }
                } else {
                    if (!hasConstructor(variableType, classCodes)) {
                        throw new RuntimeException(String.format("%s can't be used in method %s.%s", variableType.getType(), methodCode.className, methodCode.name));
                    } else {
                        //
                    }
                }
            }
        }
    }

    public static void checkClass(Map<String, ClassCode> classCodes) {
        Set<String> allClass = allClass(classCodes);
        Set<String> classCodeNames = classCodes.values().stream().map(classCode -> classCode.name).collect(Collectors.toSet());
        Collection<String> classes = CollectionUtils.removeAll(allClass, classCodeNames);
        Collection<String> classes1 = CollectionUtils.removeAll(classes, Arrays.asList(ProgramConstants.SDK_CLASS_NAMES));
        Collection<String> classes2 = CollectionUtils.removeAll(classes1, Arrays.asList(ProgramConstants.CONTRACT_USED_CLASS_NAMES));
        Collection<String> classes3 = CollectionUtils.removeAll(classes2, Arrays.asList(ProgramConstants.CONTRACT_LAZY_USED_CLASS_NAMES));
        if (classes3.size() > 0) {
            throw new RuntimeException(String.format("can't use classes: %s", Joiner.on(", ").join(classes3)));
        }
    }

    public static void checkMethod(Map<String, ClassCode> classCodes) {
        Map<String, Object> methodCodes = new HashMap(1024);
        for (ClassCode classCode : classCodes.values()) {
            for (MethodCode methodCode : classCode.methods) {
                Object o = methodCodes.get(methodCode.fullName);
                if (o == null) {
                    o = isSupportMethod(methodCode, methodCodes, classCodes);
                    methodCodes.put(methodCode.fullName, o);
                }
                if (!Boolean.TRUE.equals(o)) {
                    if (o != null) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) o;
                        throw new RuntimeException(String.format("can't use method: %s.%s%s", methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc));
                    } else {
                        throw new RuntimeException(String.format("can't use method: %s.%s%s", methodCode.className, methodCode.name, methodCode.desc));
                    }
                }
            }
        }
    }

    public static void checkOpCode(Map<String, ClassCode> classCodes) {
        for (ClassCode classCode : classCodes.values()) {
            for (MethodCode methodCode : classCode.methods) {
                checkOpCode(methodCode);
            }
        }
    }

    public static void checkOpCode(MethodCode methodCode) {
        ListIterator<AbstractInsnNode> listIterator = methodCode.instructions.iterator();
        while (listIterator.hasNext()) {
            AbstractInsnNode abstractInsnNode = listIterator.next();
            if (abstractInsnNode != null && abstractInsnNode.getOpcode() > 0) {
                OpCode opCode = OpCode.valueOf(abstractInsnNode.getOpcode());
                boolean nonsupport = false;
                if (opCode == null) {
                    nonsupport = true;
                } else {
                    switch (opCode) {
                        case JSR:
                        case RET:
                        case INVOKEDYNAMIC:
                        case MONITORENTER:
                        case MONITOREXIT:
                            nonsupport = true;
                            break;
                        default:
                            break;
                    }
                }
                if (nonsupport) {
                    int line = getLine(abstractInsnNode);
                    throw new RuntimeException(String.format("nonsupport opcode: class(%s), line(%d)", methodCode.className, line));
                }
            }
        }
    }

    public static Set<String> allClass(Map<String, ClassCode> classCodes) {
        Set<String> set = new HashSet<>(100);
        for (ClassCode classCode : classCodes.values()) {
            set.add(classCode.name);
            set.add(classCode.superName);
            set.addAll(classCode.interfaces);
            for (InnerClassNode innerClassNode : classCode.innerClasses) {
                set.add(innerClassNode.name);
            }
            for (FieldCode fieldCode : classCode.fields.values()) {
                set.add(fieldCode.desc);
            }
            for (MethodCode methodCode : classCode.methods) {
                set.addAll(allClass(methodCode));
            }
        }

        Set<String> classes = new HashSet<>(set.size() * 5);
        for (String s : set) {
            if (s == null) {
                continue;
            }
            if (s.contains("$")) {
                //continue;
            }
            if (s.contains("(")) {
                List<VariableType> list = VariableType.parseAll(s);
                for (VariableType variableType : list) {
                    if (!variableType.isPrimitiveType() && variableType.isNotVoid()) {
                        classes.add(variableType.getType());
                    }
                }
            } else {
                VariableType variableType = VariableType.valueOf(s);
                if (!variableType.isPrimitiveType() && variableType.isNotVoid()) {
                    classes.add(variableType.getType());
                }
            }
        }
        return classes;
    }

    public static Set<String> allClass(MethodCode methodCode) {
        Set<String> set = new HashSet<>();
        set.add(methodCode.returnVariableType.getType());
        for (VariableType variableType : methodCode.argsVariableType) {
            set.add(variableType.getType());
        }
        ListIterator<AbstractInsnNode> listIterator = methodCode.instructions.iterator();
        while (listIterator.hasNext()) {
            AbstractInsnNode abstractInsnNode = listIterator.next();
            if (abstractInsnNode instanceof MultiANewArrayInsnNode) {
                MultiANewArrayInsnNode insnNode = (MultiANewArrayInsnNode) abstractInsnNode;
                set.add(insnNode.desc);
            } else if (abstractInsnNode instanceof MethodInsnNode) {
                MethodInsnNode insnNode = (MethodInsnNode) abstractInsnNode;
                set.add(insnNode.owner);
                set.add(insnNode.desc);
            } else if (abstractInsnNode instanceof TypeInsnNode) {
                TypeInsnNode insnNode = (TypeInsnNode) abstractInsnNode;
                set.add(insnNode.desc);
            } else if (abstractInsnNode instanceof FieldInsnNode) {
                FieldInsnNode insnNode = (FieldInsnNode) abstractInsnNode;
                set.add(insnNode.owner);
                set.add(insnNode.desc);
            }
        }
        return set;
    }

    public static Set<String> notSupportMethods = new TreeSet<>();

    public static Object isSupportMethod(MethodCode methodCode, Map<String, Object> methodCodes, Map<String, ClassCode> classCodeMap) {
        if (!methodCodes.containsKey(methodCode.fullName)) {
            methodCodes.put(methodCode.fullName, null);
            ListIterator<AbstractInsnNode> listIterator = methodCode.instructions.iterator();
            while (listIterator.hasNext()) {
                AbstractInsnNode abstractInsnNode = listIterator.next();
                if (!(abstractInsnNode instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode methodInsnNode = (MethodInsnNode) abstractInsnNode;
                VariableType variableType = VariableType.valueOf(methodInsnNode.owner);
                if (variableType.isPrimitiveType()) {
                    continue;
                }
                ClassCode classCode = getClassCode(variableType.getType(), classCodeMap);
                if (classCode == null) {
                    log.warn("can't find class " + methodInsnNode.owner);
                    return methodInsnNode;
                }
                MethodCode methodCode1 = getMethodCode(classCode, methodInsnNode.name, methodInsnNode.desc, classCodeMap);
                if (methodCode1 == null) {
                    log.warn(String.format("can't find method %s.%s%s", methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc));
                    return methodInsnNode;
                }
                Object o = isSupportMethod(methodCode1, methodCodes, classCodeMap);
                if (!Boolean.TRUE.equals(o)) {
                    log.warn(String.format("not support method %s.%s%s", methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc));
                    return methodInsnNode;
                }
            }
        }
        return Boolean.TRUE;
    }

    public static MethodCode getMethodCode(ClassCode classCode, String methodName, String methodDesc, Map<String, ClassCode> classCodeMap) {
        MethodCode methodCode = classCode.getMethodCode(methodName, methodDesc);
        if (methodCode == null && classCode.superName != null) {
            ClassCode superClassCode = getClassCode(classCode.superName, classCodeMap);
            if (superClassCode != null) {
                methodCode = getMethodCode(superClassCode, methodName, methodDesc, classCodeMap);
            }
        }
        if (methodCode == null) {
            for (String interfaceName : classCode.interfaces) {
                ClassCode interfaceClassCode = getClassCode(interfaceName, classCodeMap);
                methodCode = getMethodCode(interfaceClassCode, methodName, methodDesc, classCodeMap);
                if (methodCode != null) {
                    break;
                }
            }
        }
        return methodCode;
    }

    public static int getLine(AbstractInsnNode abstractInsnNode) {
        while (!(abstractInsnNode instanceof LineNumberNode)) {
            abstractInsnNode = abstractInsnNode.getPrevious();
        }
        return ((LineNumberNode) abstractInsnNode).line;
    }

    public static boolean hasConstructor(VariableType variableType, Map<String, ClassCode> classCodeMap) {
        ClassCode classCode = getClassCode(variableType.getType(), classCodeMap);
        if (classCode != null) {
            MethodCode methodCode = classCode.getMethodCode("<init>", "(Ljava/lang/String;)V");
            if (methodCode != null && methodCode.isPublic) {
                return true;
            }
        }
        return false;
    }

    private static ClassCode getClassCode(String className, Map<String, ClassCode> classCodeMap) {
        ClassCode classCode = classCodeMap.get(className);
        if (classCode == null) {
            classCode = ClassCodeLoader.getFromResource(className);
        }
        return classCode;
    }

}
