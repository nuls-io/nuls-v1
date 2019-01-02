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
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeDouble {

    public static final String TYPE = "java/lang/Double";

    public static Result override(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case parseDouble:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return parseDouble(methodCode, methodArgs, frame);
                }
            case toString:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return toString(methodCode, methodArgs, frame);
                }
            case toHexString:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return toHexString(methodCode, methodArgs, frame);
                }
            default:
                return null;
        }
    }

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case doubleToRawLongBits:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return doubleToRawLongBits(methodCode, methodArgs, frame);
                }
            case longBitsToDouble:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return longBitsToDouble(methodCode, methodArgs, frame);
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

    public static final String doubleToRawLongBits = TYPE + "." + "doubleToRawLongBits" + "(D)J";

    /**
     * native
     *
     * @see Double#doubleToRawLongBits(double)
     */
    private static Result doubleToRawLongBits(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double value = (double) methodArgs.invokeArgs[0];
        long bits = Double.doubleToRawLongBits(value);
        Result result = NativeMethod.result(methodCode, bits, frame);
        return result;
    }

    public static final String longBitsToDouble = TYPE + "." + "longBitsToDouble" + "(J)D";

    /**
     * native
     *
     * @see Double#longBitsToDouble(long)
     */
    private static Result longBitsToDouble(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        long bits = (long) methodArgs.invokeArgs[0];
        double d = Double.longBitsToDouble(bits);
        Result result = NativeMethod.result(methodCode, d, frame);
        return result;
    }

    public static final String parseDouble = TYPE + "." + "parseDouble" + "(Ljava/lang/String;)D";

    /**
     * override
     *
     * @see Double#parseDouble(String)
     */
    private static Result parseDouble(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String s = frame.heap.runToString(objectRef);
        double d;
        try {
            d = Double.parseDouble(s);
        } catch (Exception e) {
            frame.throwNumberFormatException(e.getMessage());
            return null;
        }
        Result result = NativeMethod.result(methodCode, d, frame);
        return result;
    }

    public static final String toString = TYPE + "." + "toString" + "(D)Ljava/lang/String;";

    /**
     * override
     *
     * @see Double#toString(double)
     */
    private static Result toString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double d = (double) methodArgs.invokeArgs[0];
        String s = Double.toString(d);
        ObjectRef ref = frame.heap.newString(s);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

    public static final String toHexString = TYPE + "." + "toHexString" + "(D)Ljava/lang/String;";

    /**
     * override
     *
     * @see Double#toHexString(double)
     */
    private static Result toHexString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        double d = (double) methodArgs.invokeArgs[0];
        String s = Double.toHexString(d);
        ObjectRef ref = frame.heap.newString(s);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

}
