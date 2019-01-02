/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.vm.natives.java.lang;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeStrictMath {

    public static final String TYPE = "java/lang/StrictMath";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case sin:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return sin(methodCode, methodArgs, frame);
                }
            case cos:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return cos(methodCode, methodArgs, frame);
                }
            case tan:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return tan(methodCode, methodArgs, frame);
                }
            case asin:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return asin(methodCode, methodArgs, frame);
                }
            case acos:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return acos(methodCode, methodArgs, frame);
                }
            case atan:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return atan(methodCode, methodArgs, frame);
                }
            case exp:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return exp(methodCode, methodArgs, frame);
                }
            case log:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return log(methodCode, methodArgs, frame);
                }
            case log10:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return log10(methodCode, methodArgs, frame);
                }
            case sqrt:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return sqrt(methodCode, methodArgs, frame);
                }
            case cbrt:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return cbrt(methodCode, methodArgs, frame);
                }
            case IEEEremainder:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return IEEEremainder(methodCode, methodArgs, frame);
                }
            case atan2:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return atan2(methodCode, methodArgs, frame);
                }
            case pow:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return pow(methodCode, methodArgs, frame);
                }
            case sinh:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return sinh(methodCode, methodArgs, frame);
                }
            case cosh:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return cosh(methodCode, methodArgs, frame);
                }
            case tanh:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return tanh(methodCode, methodArgs, frame);
                }
            case hypot:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return hypot(methodCode, methodArgs, frame);
                }
            case expm1:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return expm1(methodCode, methodArgs, frame);
                }
            case log1p:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return log1p(methodCode, methodArgs, frame);
                }
            default:
                if (check) {
                    return NOT_SUPPORT_NATIVE;
                } else {
                    frame.nonsupportMethod(methodCode);
                    return null;
                }
        }
    }

    public static final String sin = TYPE + "." + "sin" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#sin(double)
     */
    private static Result sin(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.sin(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String cos = TYPE + "." + "cos" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#cos(double)
     */
    private static Result cos(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.cos(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String tan = TYPE + "." + "tan" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#tan(double)
     */
    private static Result tan(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.tan(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String asin = TYPE + "." + "asin" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#asin(double)
     */
    private static Result asin(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.asin(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String acos = TYPE + "." + "acos" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#acos(double)
     */
    private static Result acos(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.acos(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String atan = TYPE + "." + "atan" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#atan(double)
     */
    private static Result atan(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.atan(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String exp = TYPE + "." + "exp" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#exp(double)
     */
    private static Result exp(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.exp(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String log = TYPE + "." + "log" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#log(double)
     */
    private static Result log(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.log(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String log10 = TYPE + "." + "log10" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#log10(double)
     */
    private static Result log10(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.log10(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String sqrt = TYPE + "." + "sqrt" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#sqrt(double)
     */
    private static Result sqrt(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.sqrt(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String cbrt = TYPE + "." + "cbrt" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#cbrt(double)
     */
    private static Result cbrt(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.cbrt(a);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String IEEEremainder = TYPE + "." + "IEEEremainder" + "(DD)D";

    /**
     * native
     *
     * @see StrictMath#IEEEremainder(double, double)
     */
    private static Result IEEEremainder(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double f1 = (double) methodArgs.invokeArgs[0];
        double f2 = (double) methodArgs.invokeArgs[1];
        double r = StrictMath.IEEEremainder(f1, f2);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String atan2 = TYPE + "." + "atan2" + "(DD)D";

    /**
     * native
     *
     * @see StrictMath#atan2(double, double)
     */
    private static Result atan2(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double y = (double) methodArgs.invokeArgs[0];
        double x = (double) methodArgs.invokeArgs[1];
        double r = StrictMath.atan2(y, x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String pow = TYPE + "." + "pow" + "(DD)D";

    /**
     * native
     *
     * @see StrictMath#pow(double, double)
     */
    private static Result pow(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double a = (double) methodArgs.invokeArgs[0];
        double b = (double) methodArgs.invokeArgs[1];
        double r = StrictMath.pow(a, b);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String sinh = TYPE + "." + "sinh" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#sinh(double)
     */
    private static Result sinh(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.sinh(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String cosh = TYPE + "." + "cosh" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#cosh(double)
     */
    private static Result cosh(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.cosh(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String tanh = TYPE + "." + "tanh" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#tanh(double)
     */
    private static Result tanh(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.tanh(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String hypot = TYPE + "." + "hypot" + "(DD)D";

    /**
     * native
     *
     * @see StrictMath#hypot(double, double)
     */
    private static Result hypot(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double y = (double) methodArgs.invokeArgs[1];
        double r = StrictMath.hypot(x, y);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String expm1 = TYPE + "." + "expm1" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#expm1(double)
     */
    private static Result expm1(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.expm1(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

    public static final String log1p = TYPE + "." + "log1p" + "(D)D";

    /**
     * native
     *
     * @see StrictMath#log1p(double)
     */
    private static Result log1p(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double x = (double) methodArgs.invokeArgs[0];
        double r = StrictMath.log1p(x);
        Result result = NativeMethod.result(methodCode, r, frame);
        return result;
    }

}
