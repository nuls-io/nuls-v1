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
import io.nuls.contract.vm.natives.java.util.NativeLocale;
import io.nuls.contract.vm.util.Log;

public class NativeMethod {

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {

        Result result = null;

        switch (methodCode.classCode.name) {
            case NativeCharacter.TYPE:
                if (NativeCharacter.isSupport(methodCode)) {
                    result = NativeCharacter.run(methodCode, methodArgs, frame);
                    return result;
                }
                break;
            case NativeAbstractStringBuilder.TYPE:
                if (NativeAbstractStringBuilder.isSupport(methodCode)) {
                    result = NativeAbstractStringBuilder.run(methodCode, methodArgs, frame);
                    return result;
                }
                break;
            case NativeFloat.TYPE:
                if (NativeFloat.isSupport(methodCode)) {
                    result = NativeFloat.run(methodCode, methodArgs, frame);
                    return result;
                }
                break;
            case NativeDouble.TYPE:
                if (NativeDouble.isSupport(methodCode)) {
                    result = NativeDouble.run(methodCode, methodArgs, frame);
                    return result;
                }
                break;
//            case NativeLocale.TYPE:
//                if (NativeLocale.isSupport(methodCode)) {
//                    result = NativeLocale.run(methodCode, methodArgs, frame);
//                    return result;
//                }
//                break;
            case NativeClass.TYPE:
                if (NativeClass.isSupport(methodCode)) {
                    result = NativeClass.run(methodCode, methodArgs, frame);
                    return result;
                }
                break;
            default:
                break;
        }

        if (methodCode.instructions.size() > 0) {
            return null;
        }

        if ("registerNatives".equals(methodCode.name)) {
            return new Result(methodCode.returnVariableType);
        }

        //Log.nativeMethod(methodCode);

        switch (methodCode.classCode.name) {
            case NativeClass.TYPE:
                result = NativeClass.run(methodCode, methodArgs, frame);
                break;
            case NativeDouble.TYPE:
                result = NativeDouble.run(methodCode, methodArgs, frame);
                break;
            case NativeFloat.TYPE:
                result = NativeFloat.run(methodCode, methodArgs, frame);
                break;
            case NativeObject.TYPE:
                result = NativeObject.run(methodCode, methodArgs, frame);
                break;
            case NativeSystem.TYPE:
                result = NativeSystem.run(methodCode, methodArgs, frame);
                break;
            case NativeStrictMath.TYPE:
                result = NativeStrictMath.run(methodCode, methodArgs, frame);
                break;
            case NativeString.TYPE:
                result = NativeString.run(methodCode, methodArgs, frame);
                break;
            case NativeThrowable.TYPE:
                result = NativeThrowable.run(methodCode, methodArgs, frame);
                break;
            case NativeBlock.TYPE:
                result = NativeBlock.run(methodCode, methodArgs, frame);
                break;
            case NativeUtils.TYPE:
                result = NativeUtils.run(methodCode, methodArgs, frame);
                break;
            case NativeMsg.TYPE:
                result = NativeMsg.run(methodCode, methodArgs, frame);
                break;
            case NativeAddress.TYPE:
                result = NativeAddress.run(methodCode, methodArgs, frame);
                break;
            case NativeArray.TYPE:
                result = NativeArray.run(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }

        //Log.nativeMethodResult(result);

        return result;
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

    public static final String[] SUPPORT_CLASSES = new String[]{
            NativeAddress.TYPE,
            NativeBlock.TYPE,
            NativeMsg.TYPE,
            NativeUtils.TYPE,
//            NativeClass.TYPE,
            NativeDouble.TYPE,
            NativeFloat.TYPE,
            NativeObject.TYPE,
            NativeStrictMath.TYPE,
            NativeString.TYPE,
//            NativeSystem.TYPE,
            NativeThrowable.TYPE,
//            "java/security/AccessController",
    };

    public static boolean isSupport(MethodCode methodCode) {
        if (NativeArray.isSupport(methodCode)
                || NativeAbstractStringBuilder.isSupport(methodCode)
                || NativeClass.isSupport(methodCode)
                || NativeDouble.isSupport(methodCode)
                || NativeFloat.isSupport(methodCode)
                || NativeSystem.isSupport(methodCode)
//                || NativeLocale.isSupport(methodCode)
//                || NativeUnsafe.isSupport(methodCode)
        ) {
            return true;
        } else {
            return false;
        }
    }

}
