package io.nuls.contract.vm.natives.java.sun.misc;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeUnsafe {

    public static final String TYPE = "sun/misc/Unsafe";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && (methodCode.isMethod("compareAndSwapLong", "(Ljava/lang/Object;JJJ)Z")
                || methodCode.isMethod("compareAndSwapObject", "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z")
        )) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "compareAndSwapLong":
                result = compareAndSwapLong(methodCode, methodArgs, frame);
                break;
            case "compareAndSwapObject":
                result = compareAndSwapObject(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result compareAndSwapLong(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        int bits = (int) methodArgs.invokeArgs[0];
        float f = Float.intBitsToFloat(bits);
        Result result = NativeMethod.result(methodCode, f, frame);
        return result;
    }

    private static Result compareAndSwapObject(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        float value = (float) methodArgs.invokeArgs[0];
        int bits = Float.floatToRawIntBits(value);
        Result result = NativeMethod.result(methodCode, bits, frame);
        return result;
    }

}
