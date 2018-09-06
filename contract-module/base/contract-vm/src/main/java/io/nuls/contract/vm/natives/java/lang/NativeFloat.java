package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeFloat {

    public static final String TYPE = "java/lang/Float";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && (methodCode.isMethod("parseFloat", "(Ljava/lang/String;)F")
                || methodCode.isMethod("toString", "(F)Ljava/lang/String;")
                || methodCode.isMethod("toHexString", "(F)Ljava/lang/String;")
        )) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.getName()) {
            case "intBitsToFloat":
                result = intBitsToFloat(methodCode, methodArgs, frame);
                break;
            case "floatToRawIntBits":
                result = floatToRawIntBits(methodCode, methodArgs, frame);
                break;
            case "parseFloat":
                result = parseFloat(methodCode, methodArgs, frame);
                break;
            case "toString":
                result = toString(methodCode, methodArgs, frame);
                break;
            case "toHexString":
                result = toHexString(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result intBitsToFloat(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        int bits = (int) methodArgs.getInvokeArgs()[0];
        float f = Float.intBitsToFloat(bits);
        Result result = NativeMethod.result(methodCode, f, frame);
        return result;
    }

    private static Result floatToRawIntBits(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        float value = (float) methodArgs.getInvokeArgs()[0];
        int bits = Float.floatToRawIntBits(value);
        Result result = NativeMethod.result(methodCode, bits, frame);
        return result;
    }

    private static Result parseFloat(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
        String s = frame.getHeap().runToString(objectRef);
        float f;
        try {
            f = Float.parseFloat(s);
        } catch (Exception e) {
            frame.throwNumberFormatException(e.getMessage());
            return null;
        }
        Result result = NativeMethod.result(methodCode, f, frame);
        return result;
    }

    private static Result toString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        float f = (float) methodArgs.getInvokeArgs()[0];
        String s = Float.toString(f);
        ObjectRef ref = frame.getHeap().newString(s);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

    private static Result toHexString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        float f = (float) methodArgs.getInvokeArgs()[0];
        String s = Float.toHexString(f);
        ObjectRef ref = frame.getHeap().newString(s);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

}
