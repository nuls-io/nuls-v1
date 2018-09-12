package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeAbstractStringBuilder {

    public static final String TYPE = "java/lang/AbstractStringBuilder";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && (methodCode.isMethod("append", "(D)Ljava/lang/AbstractStringBuilder;")
                || methodCode.isMethod("append", "(F)Ljava/lang/AbstractStringBuilder;"))) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "append":
                result = append(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result append(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        Object a = methodArgs.invokeArgs[0];
        ObjectRef ref = frame.heap.newString(a.toString());
        MethodCode append = frame.methodArea.loadMethod(TYPE, "append", "(Ljava/lang/String;)Ljava/lang/AbstractStringBuilder;");
        frame.vm.run(append, new Object[]{objectRef, ref}, false);
        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

}
