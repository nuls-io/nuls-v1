package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeSystem {

    public static final String TYPE = "java/lang/System";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && (methodCode.isMethod("arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V")
                || methodCode.isMethod("getProperty", "(Ljava/lang/String;)Ljava/lang/String;")
                || methodCode.isMethod("getProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.getName()) {
            case "arraycopy":
                result = arraycopy(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result arraycopy(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Object[] args = methodArgs.getInvokeArgs();
        ObjectRef srcObjectRef = (ObjectRef) args[0];
        int srcPos = (int) args[1];
        ObjectRef destObjectRef = (ObjectRef) args[2];
        int destPos = (int) args[3];
        int length = (int) args[4];

        if (length > 0 && frame.checkArray(srcObjectRef, srcPos)
                && frame.checkArray(srcObjectRef, srcPos + length - 1)
                && frame.checkArray(destObjectRef, destPos)
                && frame.checkArray(destObjectRef, destPos + length - 1)) {
            frame.getHeap().arraycopy(srcObjectRef, srcPos, destObjectRef, destPos, length);
        }

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

}
