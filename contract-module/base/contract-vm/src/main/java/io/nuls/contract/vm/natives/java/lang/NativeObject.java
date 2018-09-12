package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.util.CloneUtils;

import java.util.Map;

public class NativeObject {

    public static final String TYPE = "java/lang/Object";

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "getClass":
                result = getClass(methodCode, methodArgs, frame);
                break;
            case "hashCode":
                result = hashCode(methodCode, methodArgs, frame);
                break;
            case "clone":
                result = clone(methodCode, methodArgs, frame);
                break;
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
        ObjectRef objectRef = methodArgs.objectRef;
        ObjectRef classRef = frame.heap.getClassRef(objectRef.getVariableType().getDesc());
        Result result = NativeMethod.result(methodCode, classRef, frame);
        return result;
    }

    private static Result hashCode(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        int hashCode = objectRef.hashCode();
        Result result = NativeMethod.result(methodCode, hashCode, frame);
        return result;
    }

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
