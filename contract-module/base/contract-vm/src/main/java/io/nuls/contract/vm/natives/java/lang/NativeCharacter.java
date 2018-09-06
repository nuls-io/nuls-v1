package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeCharacter {

    public static final String TYPE = "java/lang/Character";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && (methodCode.isMethod("digit", "(II)I"))) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.getName()) {
            case "digit":
                result = digit(methodCode, methodArgs, frame);
                break;
            default:
                break;
        }
        return result;
    }

    private static Result digit(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Object first = methodArgs.getInvokeArgs()[0];
        int codePoint;
        if (first instanceof Character) {
            codePoint = (int) ((Character) first).charValue();
        } else {
            codePoint = (int) first;
        }
        int radix = (int) methodArgs.getInvokeArgs()[1];
        int i = Character.digit(codePoint, radix);
        Result result = NativeMethod.result(methodCode, i, frame);
        return result;
    }

}
