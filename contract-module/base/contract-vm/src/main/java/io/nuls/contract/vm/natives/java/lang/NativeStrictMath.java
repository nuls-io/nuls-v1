package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

public class NativeStrictMath {

    public static final String TYPE = "java/lang/StrictMath";

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Object invokeResult = null;
        try {
            invokeResult = MethodUtils.invokeStaticMethod(StrictMath.class, methodCode.name, methodArgs.invokeArgs);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        Result result = NativeMethod.result(methodCode, invokeResult, frame);
        return result;
    }

    public static Result run1(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "sin":
                result = sin(methodCode, methodArgs, frame);
                break;
            case "cos":
                result = cos(methodCode, methodArgs, frame);
                break;
            case "tan":
                result = tan(methodCode, methodArgs, frame);
                break;
            case "asin":
                result = asin(methodCode, methodArgs, frame);
                break;
            case "acos":
                result = acos(methodCode, methodArgs, frame);
                break;
            case "atan":
                result = atan(methodCode, methodArgs, frame);
                break;
            case "exp":
                result = exp(methodCode, methodArgs, frame);
                break;
            case "log":
                result = log(methodCode, methodArgs, frame);
                break;
            case "log10":
                result = log10(methodCode, methodArgs, frame);
                break;
            case "sqrt":
                result = sqrt(methodCode, methodArgs, frame);
                break;
            case "cbrt":
                result = cbrt(methodCode, methodArgs, frame);
                break;
            case "IEEEremainder":
                result = IEEEremainder(methodCode, methodArgs, frame);
                break;
            case "atan2":
                result = atan2(methodCode, methodArgs, frame);
                break;
            case "pow":
                result = pow(methodCode, methodArgs, frame);
                break;
            case "sinh":
                result = sinh(methodCode, methodArgs, frame);
                break;
            case "cosh":
                result = cosh(methodCode, methodArgs, frame);
                break;
            case "tanh":
                result = tanh(methodCode, methodArgs, frame);
                break;
            case "hypot":
                result = hypot(methodCode, methodArgs, frame);
                break;
            case "expm1":
                result = expm1(methodCode, methodArgs, frame);
                break;
            case "log1p":
                result = log1p(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result sin(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.sin(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result cos(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.cos(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result tan(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.tan(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result asin(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.asin(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result acos(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.acos(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result atan(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.atan(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result exp(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.exp(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result log(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.log(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result log10(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.log10(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result sqrt(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.sqrt(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result cbrt(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.cbrt(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result IEEEremainder(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double f1 = (double) methodArgs.invokeArgs[0];
        double f2 = (double) methodArgs.invokeArgs[1];
        double r = StrictMath.IEEEremainder(f1, f2);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result atan2(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double y = (double) methodArgs.invokeArgs[0];
        double x = (double) methodArgs.invokeArgs[1];
        double r = StrictMath.atan2(y, x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result pow(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double b = (double) methodArgs.invokeArgs[1];
        double r = StrictMath.pow(a, b);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result sinh(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.sinh(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result cosh(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.cosh(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result tanh(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.tanh(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result hypot(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double y = (double) methodArgs.invokeArgs[1];
        double r = StrictMath.hypot(x, y);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result expm1(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.expm1(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    private static Result log1p(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.log1p(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

}
