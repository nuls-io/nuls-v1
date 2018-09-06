package io.nuls.contract.vm.natives.java.lang.reflect;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeArray {

    public static final String TYPE = "java/lang/reflect/Array";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && (methodCode.isMethod("newArray", "(Ljava/lang/Class;I)Ljava/lang/Object;"))) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.getName()) {
            case "newArray":
                result = newArray(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result newArray(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef componentType = (ObjectRef) methodArgs.getInvokeArgs()[0];
        int length = (int) methodArgs.getInvokeArgs()[1];
        VariableType variableType = VariableType.valueOf("[" + componentType.getRef());
        ObjectRef array = frame.getHeap().newArray(variableType, length);
        Result result = NativeMethod.result(methodCode, array, frame);
        return result;
    }

}
