package io.nuls.contract.vm.natives;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeBlock;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeMsg;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeUtils;
import io.nuls.contract.vm.natives.java.lang.*;
import io.nuls.contract.vm.natives.java.lang.reflect.NativeArray;

public class NativeMethod {

    private static final String registerNatives = "registerNatives";

    public static final Result SUCCESS = new Result();

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {

        Result result = null;

        switch (methodCode.className) {
            case NativeAbstractStringBuilder.TYPE:
                result = NativeAbstractStringBuilder.override(methodCode, methodArgs, frame, check);
                break;
            case NativeCharacter.TYPE:
                result = NativeCharacter.override(methodCode, methodArgs, frame, check);
                break;
            case NativeClass.TYPE:
                result = NativeClass.override(methodCode, methodArgs, frame, check);
                break;
            case NativeDouble.TYPE:
                result = NativeDouble.override(methodCode, methodArgs, frame, check);
                break;
            case NativeFloat.TYPE:
                result = NativeFloat.override(methodCode, methodArgs, frame, check);
                break;
            default:
                break;
        }

        if (result != null) {
            return result;
        }

        if (methodCode.instructions.size() > 0) {
            return null;
        }

        if (registerNatives.equals(methodCode.name)) {
            return new Result(methodCode.returnVariableType);
        }

        //Log.nativeMethod(methodCode);

        switch (methodCode.className) {
            case NativeArray.TYPE:
                result = NativeArray.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeClass.TYPE:
                result = NativeClass.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeDouble.TYPE:
                result = NativeDouble.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeFloat.TYPE:
                result = NativeFloat.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeObject.TYPE:
                result = NativeObject.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeStrictMath.TYPE:
                result = NativeStrictMath.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeString.TYPE:
                result = NativeString.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeSystem.TYPE:
                result = NativeSystem.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeThrowable.TYPE:
                result = NativeThrowable.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeAddress.TYPE:
                result = NativeAddress.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeBlock.TYPE:
                result = NativeBlock.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeMsg.TYPE:
                result = NativeMsg.nativeRun(methodCode, methodArgs, frame, check);
                break;
            case NativeUtils.TYPE:
                result = NativeUtils.nativeRun(methodCode, methodArgs, frame, check);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }

        //Log.nativeMethodResult(result);

        return result;
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        return run(methodCode, methodArgs, frame, false);
    }

    public static Result result(MethodCode methodCode, Object resultValue, Frame frame) {
        VariableType variableType = methodCode.returnVariableType;
        Result result = new Result(variableType);
        if (variableType.isNotVoid()) {
            result.value(resultValue);
            if (resultValue == null) {
                frame.operandStack.pushRef(null);
            } else if (variableType.isPrimitive()) {
                frame.operandStack.push(resultValue, variableType);
            } else if (resultValue instanceof ObjectRef) {
                frame.operandStack.pushRef((ObjectRef) resultValue);
            } else {
                throw new IllegalArgumentException("unknown result value");
            }
        }
        return result;
    }

}
