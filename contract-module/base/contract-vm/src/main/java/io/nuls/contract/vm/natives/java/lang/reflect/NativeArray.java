package io.nuls.contract.vm.natives.java.lang.reflect;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.natives.NativeMethod;

import java.lang.reflect.Array;

import static io.nuls.contract.vm.natives.NativeMethod.SUCCESS;

public class NativeArray {

    public static final String TYPE = "java/lang/reflect/Array";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case newArray:
                if (check) {
                    return SUCCESS;
                } else {
                    return newArray(methodCode, methodArgs, frame);
                }
            default:
                frame.nonsupportMethod(methodCode);
                return null;
        }
    }

    public static final String newArray = TYPE + "." + "newArray" + "(Ljava/lang/Class;I)Ljava/lang/Object;";

    /**
     * native
     *
     * @see Array#newArray(Class, int)
     */
    private static Result newArray(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef componentType = (ObjectRef) methodArgs.invokeArgs[0];
        int length = (int) methodArgs.invokeArgs[1];
        VariableType variableType = VariableType.valueOf("[" + componentType.getRef());
        ObjectRef array = frame.heap.newArray(variableType, length);
        Result result = NativeMethod.result(methodCode, array, frame);
        return result;
    }

}
