package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.util.CloneUtils;

import java.util.Map;

import static io.nuls.contract.vm.natives.NativeMethod.SUCCESS;

public class NativeObject {

    public static final String TYPE = "java/lang/Object";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case getClass:
                if (check) {
                    return SUCCESS;
                } else {
                    return getClass(methodCode, methodArgs, frame);
                }
            case hashCode:
                if (check) {
                    return SUCCESS;
                } else {
                    return hashCode(methodCode, methodArgs, frame);
                }
            case clone:
                if (check) {
                    return SUCCESS;
                } else {
                    return clone(methodCode, methodArgs, frame);
                }
            default:
                frame.nonsupportMethod(methodCode);
                return null;
        }
    }

    public static final String getClass = TYPE + "." + "getClass" + "()Ljava/lang/Class;";

    /**
     * native
     *
     * @see Object#getClass()
     */
    private static Result getClass(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        ObjectRef classRef = frame.heap.getClassRef(objectRef.getVariableType().getDesc());
        Result result = NativeMethod.result(methodCode, classRef, frame);
        return result;
    }

    public static final String hashCode = TYPE + "." + "hashCode" + "()I";

    /**
     * native
     *
     * @see Object#hashCode()
     */
    private static Result hashCode(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        int hashCode = objectRef.hashCode();
        Result result = NativeMethod.result(methodCode, hashCode, frame);
        return result;
    }

    public static final String clone = TYPE + "." + "clone" + "()Ljava/lang/Object;";

    /**
     * native
     *
     * @see Object#clone()
     */
    private static Result clone(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        Map<String, Object> fields = frame.heap.getFields(objectRef);
        Map<String, Object> newFields = CloneUtils.clone(fields);
        ObjectRef newRef = frame.heap.newObjectRef(null, objectRef.getDesc(), objectRef.getDimensions());
        frame.heap.putFields(newRef, newFields);
        Result result = NativeMethod.result(methodCode, newRef, frame);
        return result;
    }

}
