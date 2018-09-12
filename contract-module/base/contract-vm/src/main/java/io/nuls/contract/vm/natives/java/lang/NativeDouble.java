package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeDouble {

    public static final String TYPE = "java/lang/Double";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && (methodCode.isMethod("parseDouble", "(Ljava/lang/String;)D")
                || methodCode.isMethod("toString", "(D)Ljava/lang/String;")
                || methodCode.isMethod("toHexString", "(D)Ljava/lang/String;")
        )) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "doubleToRawLongBits":
                result = doubleToRawLongBits(methodCode, methodArgs, frame);
                break;
            case "longBitsToDouble":
                result = longBitsToDouble(methodCode, methodArgs, frame);
                break;
            case "parseDouble":
                result = parseDouble(methodCode, methodArgs, frame);
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

    private static Result doubleToRawLongBits(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double value = (double) methodArgs.invokeArgs[0];
        long bits = Double.doubleToRawLongBits(value);
        Result result = NativeMethod.result(methodCode, bits, frame);
        return result;
    }

    private static Result longBitsToDouble(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        long bits = (long) methodArgs.invokeArgs[0];
        double d = Double.longBitsToDouble(bits);
        Result result = NativeMethod.result(methodCode, d, frame);
        return result;
    }

    private static Result parseDouble(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String s = frame.heap.runToString(objectRef);
        double d;
        try {
            d = Double.parseDouble(s);
        } catch (Exception e) {
            frame.throwNumberFormatException(e.getMessage());
            return null;
        }
        Result result = NativeMethod.result(methodCode, d, frame);
        return result;
    }

    private static Result toString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double d = (double) methodArgs.invokeArgs[0];
        String s = Double.toString(d);
        ObjectRef ref = frame.heap.newString(s);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

    private static Result toHexString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double d = (double) methodArgs.invokeArgs[0];
        String s = Double.toHexString(d);
        ObjectRef ref = frame.heap.newString(s);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

}
