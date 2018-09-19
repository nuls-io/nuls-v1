package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

import static io.nuls.contract.vm.natives.NativeMethod.SUCCESS;

public class NativeAbstractStringBuilder {

    public static final String TYPE = "java/lang/AbstractStringBuilder";

    public static final String appendD = TYPE + "." + "append" + "(D)Ljava/lang/AbstractStringBuilder;";

    public static final String appendF = TYPE + "." + "append" + "(F)Ljava/lang/AbstractStringBuilder;";

    public static Result override(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case appendD:
                if (check) {
                    return SUCCESS;
                } else {
                    return append(methodCode, methodArgs, frame);
                }
            case appendF:
                if (check) {
                    return SUCCESS;
                } else {
                    return append(methodCode, methodArgs, frame);
                }
            default:
                return null;
        }
    }

    /**
     * override
     *
     * @see AbstractStringBuilder#append(float)
     * @see AbstractStringBuilder#append(double)
     */
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
