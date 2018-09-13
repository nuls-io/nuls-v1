package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

import static io.nuls.contract.vm.natives.NativeMethod.SUCCESS;

public class NativeCharacter {

    public static final String TYPE = "java/lang/Character";

    public static final String digit = TYPE + "." + "digit" + "(II)I";

    public static Result override(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case digit:
                if (check) {
                    return SUCCESS;
                } else {
                    return digit(methodCode, methodArgs, frame);
                }
            default:
                return null;
        }
    }

    /**
     * override
     *
     * @see Character#digit(int, int)
     */
    private static Result digit(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        int codePoint = (int) methodArgs.invokeArgs[0];
        int radix = (int) methodArgs.invokeArgs[1];
        int i = Character.digit(codePoint, radix);
        Result result = NativeMethod.result(methodCode, i, frame);
        return result;
    }

}
