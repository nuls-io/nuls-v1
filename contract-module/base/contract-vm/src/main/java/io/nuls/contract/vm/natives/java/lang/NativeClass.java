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

public class NativeClass {

    public static final String TYPE = "java/lang/Class";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && (methodCode.isMethod("getPrimitiveClass", "(Ljava/lang/String;)Ljava/lang/Class;")
                || methodCode.isMethod("getComponentType", "()Ljava/lang/Class;")
                || methodCode.isMethod("isArray", "()Z")
                || methodCode.isMethod("isPrimitive", "()Z")
                || methodCode.isMethod("isInterface", "()Z")
                || methodCode.isMethod("desiredAssertionStatus0", "(Ljava/lang/Class;)Z")
                || methodCode.isMethod("getGenericSignature0", "()Ljava/lang/String;")
                || methodCode.isMethod("getName0", "()Ljava/lang/String;")
                || methodCode.isMethod("getInterfaces", "()[Ljava/lang/Class;")
        )) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "getPrimitiveClass":
                result = getPrimitiveClass(methodCode, methodArgs, frame);
                break;
            case "getComponentType":
                result = getComponentType(methodCode, methodArgs, frame);
                break;
            case "isArray":
                result = isArray(methodCode, methodArgs, frame);
                break;
            case "isPrimitive":
                result = isPrimitive(methodCode, methodArgs, frame);
                break;
            case "isInterface":
                result = isInterface(methodCode, methodArgs, frame);
                break;
            case "desiredAssertionStatus0":
                result = desiredAssertionStatus0(methodCode, methodArgs, frame);
                break;
            case "getGenericSignature0":
                result = getGenericSignature0(methodCode, methodArgs, frame);
                break;
            case "getName0":
                result = getName0(methodCode, methodArgs, frame);
                break;
            case "getInterfaces":
                result = getInterfaces(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result getPrimitiveClass(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String name = frame.heap.runToString(objectRef);
        VariableType variableType = VariableType.valueOf(name);
        ObjectRef classRef = frame.heap.getClassRef(variableType.getDesc());
        Result result = NativeMethod.result(methodCode, classRef, frame);
        return result;
    }

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

    private static Result desiredAssertionStatus0(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        boolean status = false;
        Result result = NativeMethod.result(methodCode, status, frame);
        return result;
    }

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

}
