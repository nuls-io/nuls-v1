package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeString {

    public static final String TYPE = "java/lang/String";

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "intern":
                result = intern(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result intern(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

}
