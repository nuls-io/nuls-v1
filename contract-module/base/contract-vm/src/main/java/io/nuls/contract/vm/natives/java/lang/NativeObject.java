package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeObject {

    public static final String TYPE = "java/lang/Object";

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.getName()) {
            case "getClass":
                result = getClass(methodCode, methodArgs, frame);
                break;
            case "hashCode":
                result = hashCode(methodCode, methodArgs, frame);
                break;
            case "clone":
            case "notify":
            case "notifyAll":
            case "wait":
                frame.nonsupportMethod(methodCode);
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result getClass(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.getObjectRef();
        ObjectRef classRef = frame.getHeap().getClassRef(objectRef.getVariableType().getDesc());
        Result result = NativeMethod.result(methodCode, classRef, frame);
        return result;
    }

    private static Result hashCode(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.getObjectRef();
        int hashCode = objectRef.hashCode();
        Result result = NativeMethod.result(methodCode, hashCode, frame);
        return result;
    }

}
