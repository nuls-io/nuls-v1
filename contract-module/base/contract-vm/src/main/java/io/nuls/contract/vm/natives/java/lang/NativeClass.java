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
package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.natives.NativeMethod;

import java.util.List;

import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeClass {

    public static final String TYPE = "java/lang/Class";

    public static Result override(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case getInterfaces:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getInterfaces(methodCode, methodArgs, frame);
                }
            case desiredAssertionStatus:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return desiredAssertionStatus(methodCode, methodArgs, frame);
                }
            default:
                return null;
        }
    }

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case getPrimitiveClass:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getPrimitiveClass(methodCode, methodArgs, frame);
                }
            case getComponentType:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getComponentType(methodCode, methodArgs, frame);
                }
            case isArray:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return isArray(methodCode, methodArgs, frame);
                }
            case isPrimitive:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return isPrimitive(methodCode, methodArgs, frame);
                }
            case isInterface:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return isInterface(methodCode, methodArgs, frame);
                }
            case desiredAssertionStatus0:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return desiredAssertionStatus0(methodCode, methodArgs, frame);
                }
            case getGenericSignature0:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getGenericSignature0(methodCode, methodArgs, frame);
                }
            case getName0:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getName0(methodCode, methodArgs, frame);
                }
            case getSuperclass:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getSuperclass(methodCode, methodArgs, frame);
                }
            default:
                if (check) {
                    return NOT_SUPPORT_NATIVE;
                } else {
                    frame.nonsupportMethod(methodCode);
                    return null;
                }
        }
    }

    public static final String getPrimitiveClass = TYPE + "." + "getPrimitiveClass" + "(Ljava/lang/String;)Ljava/lang/Class;";

    /**
     * native
     *
     * @see Class#getPrimitiveClass(String)
     */
    private static Result getPrimitiveClass(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String name = frame.heap.runToString(objectRef);
        VariableType variableType = VariableType.valueOf(name);
        ObjectRef classRef = frame.heap.getClassRef(variableType.getDesc());
        Result result = NativeMethod.result(methodCode, classRef, frame);
        return result;
    }

    public static final String getComponentType = TYPE + "." + "getComponentType" + "()Ljava/lang/Class;";

    /**
     * native
     *
     * @see Class#getComponentType()
     */
    private static Result getComponentType(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        VariableType variableType;
        if (objectRef.getVariableType().isArray()) {
            variableType = objectRef.getVariableType();
        } else {
            variableType = VariableType.valueOf(objectRef.getRef());
        }
        ObjectRef classRef = null;
        if (variableType.isArray()) {
            classRef = frame.heap.getClassRef(variableType.getComponentType().getDesc());
        }
        Result result = NativeMethod.result(methodCode, classRef, frame);
        return result;
    }

    public static final String isArray = TYPE + "." + "isArray" + "()Z";

    /**
     * native
     *
     * @see Class#isArray()
     */
    private static Result isArray(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        VariableType variableType;
        if (objectRef.getVariableType().isArray()) {
            variableType = objectRef.getVariableType();
        } else {
            variableType = VariableType.valueOf(objectRef.getRef());
        }
        boolean b = variableType.isArray();
        Result result = NativeMethod.result(methodCode, b, frame);
        return result;
    }

    public static final String isPrimitive = TYPE + "." + "isPrimitive" + "()Z";

    /**
     * native
     *
     * @see Class#isPrimitive()
     */
    private static Result isPrimitive(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        VariableType variableType;
        if (objectRef.getVariableType().isArray()) {
            variableType = objectRef.getVariableType();
        } else {
            variableType = VariableType.valueOf(objectRef.getRef());
        }
        boolean b = variableType.isPrimitive();
        Result result = NativeMethod.result(methodCode, b, frame);
        return result;
    }

    public static final String isInterface = TYPE + "." + "isInterface" + "()Z";

    /**
     * native
     *
     * @see Class#isInterface()
     */
    private static Result isInterface(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        VariableType variableType;
        if (objectRef.getVariableType().isArray()) {
            variableType = objectRef.getVariableType();
        } else {
            variableType = VariableType.valueOf(objectRef.getRef());
        }
        boolean b = false;
        if (!variableType.isArray() && !variableType.isPrimitiveType()) {
            ClassCode classCode = frame.methodArea.loadClass(variableType.getType());
            b = classCode.isInterface;
        }
        Result result = NativeMethod.result(methodCode, b, frame);
        return result;
    }

    public static final String desiredAssertionStatus = TYPE + "." + "desiredAssertionStatus" + "()Z";

    /**
     * override
     *
     * @see Class#desiredAssertionStatus()
     */
    private static Result desiredAssertionStatus(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        boolean status = false;
        Result result = NativeMethod.result(methodCode, status, frame);
        return result;
    }

    public static final String desiredAssertionStatus0 = TYPE + "." + "desiredAssertionStatus0" + "(Ljava/lang/Class;)Z";

    /**
     * native
     *
     * @see Class#desiredAssertionStatus0(Class)
     */
    private static Result desiredAssertionStatus0(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        boolean status = false;
        Result result = NativeMethod.result(methodCode, status, frame);
        return result;
    }

    public static final String getGenericSignature0 = TYPE + "." + "getGenericSignature0" + "()Ljava/lang/String;";

    /**
     * native
     *
     * @see Class#getGenericSignature0()
     */
    private static Result getGenericSignature0(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        VariableType variableType;
        if (objectRef.getVariableType().isArray()) {
            variableType = objectRef.getVariableType();
        } else {
            variableType = VariableType.valueOf(objectRef.getRef());
        }
        ObjectRef ref = null;
        if (!variableType.isPrimitiveType()) {
            ClassCode classCode = frame.methodArea.loadClass(variableType.getType());
            String signature = classCode.signature;
            ref = frame.heap.newString(signature);
        }
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

    public static final String getName0 = TYPE + "." + "getName0" + "()Ljava/lang/String;";

    /**
     * native
     *
     * @see Class#getName0()
     */
    private static Result getName0(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        VariableType variableType;
        if (objectRef.getVariableType().isArray()) {
            variableType = objectRef.getVariableType();
        } else {
            variableType = VariableType.valueOf(objectRef.getRef());
        }
        String name;
        if (variableType.isArray()) {
            name = variableType.getDesc();
        } else {
            name = variableType.getType();
            if (name.startsWith("L") && name.endsWith(";")) {
                name = name.substring(1, name.length() - 1);
            }
        }
        name = name.replace('/', '.');
        ObjectRef ref = frame.heap.newString(name);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

    public static final String getInterfaces = TYPE + "." + "getInterfaces" + "()[Ljava/lang/Class;";

    /**
     * override
     *
     * @see Class#getInterfaces()
     */
    private static Result getInterfaces(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        VariableType variableType;
        if (objectRef.getVariableType().isArray()) {
            variableType = objectRef.getVariableType();
        } else {
            variableType = VariableType.valueOf(objectRef.getRef());
        }
        ObjectRef array;
        if (!variableType.isPrimitiveType()) {
            ClassCode classCode = frame.methodArea.loadClass(variableType.getType());
            List<String> interfaces = classCode.interfaces;
            int length = interfaces.size();
            array = frame.heap.newArray(VariableType.valueOf("[Ljava/lang/Class;"), length);
            for (int i = 0; i < length; i++) {
                String interfaceName = interfaces.get(i);
                VariableType interfaceType = VariableType.valueOf(interfaceName);
                ObjectRef ref = frame.heap.getClassRef(interfaceType.getDesc());
                frame.heap.putArray(array, i, ref);
            }
        } else {
            array = frame.heap.newArray(VariableType.valueOf("[Ljava/lang/Class;"), 0);
        }
        Result result = NativeMethod.result(methodCode, array, frame);
        return result;
    }

    public static final String getSuperclass = TYPE + "." + "getSuperclass" + "()Ljava/lang/Class;";

    /**
     * native
     *
     * @see Class#getSuperclass()
     */
    private static Result getSuperclass(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        VariableType variableType;
        if (objectRef.getVariableType().isArray()) {
            variableType = objectRef.getVariableType();
        } else {
            variableType = VariableType.valueOf(objectRef.getRef());
        }
        ClassCode classCode = frame.methodArea.loadClass(variableType.getType());
        VariableType superVariableType = VariableType.valueOf(classCode.superName);
        ObjectRef classRef = frame.heap.getClassRef(superVariableType.getDesc());
        Result result = NativeMethod.result(methodCode, classRef, frame);
        return result;
    }

}
