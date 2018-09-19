package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

import static io.nuls.contract.vm.natives.NativeMethod.SUCCESS;

public class NativeSystem {

    public static final String TYPE = "java/lang/System";

    public static final String getProperty = TYPE + "." + "getProperty" + "(Ljava/lang/String;)Ljava/lang/String;";

    public static final String getProperty_ = TYPE + "." + "getProperty" + "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case arraycopy:
                if (check) {
                    return SUCCESS;
                } else {
                    return arraycopy(methodCode, methodArgs, frame);
                }
            default:
                frame.nonsupportMethod(methodCode);
                return null;
        }
    }

    public static final String arraycopy = TYPE + "." + "arraycopy" + "(Ljava/lang/Object;ILjava/lang/Object;II)V";

    /**
     * native
     *
     * @see System#arraycopy(Object, int, Object, int, int)
     */
    private static Result arraycopy(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Object[] args = methodArgs.invokeArgs;
        ObjectRef srcObjectRef = (ObjectRef) args[0];
        int srcPos = (int) args[1];
        ObjectRef destObjectRef = (ObjectRef) args[2];
        int destPos = (int) args[3];
        int length = (int) args[4];

        if (length > 0 && frame.checkArray(srcObjectRef, srcPos)
                && frame.checkArray(srcObjectRef, srcPos + length - 1)
                && frame.checkArray(destObjectRef, destPos)
                && frame.checkArray(destObjectRef, destPos + length - 1)) {
            frame.heap.arraycopy(srcObjectRef, srcPos, destObjectRef, destPos, length);
        }

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

}
