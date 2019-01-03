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

public class NativeFloat {

    public static final String TYPE = "java/lang/Float";

    public static Result override(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case parseFloat:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return parseFloat(methodCode, methodArgs, frame);
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
            case intBitsToFloat:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return intBitsToFloat(methodCode, methodArgs, frame);
                }
            case floatToRawIntBits:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return floatToRawIntBits(methodCode, methodArgs, frame);
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

    public static final String intBitsToFloat = TYPE + "." + "intBitsToFloat" + "(I)F";

    /**
     * native
     *
     * @see Float#intBitsToFloat(int)
     */
    private static Result intBitsToFloat(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        int bits = (int) methodArgs.invokeArgs[0];
        float f = Float.intBitsToFloat(bits);
        Result result = NativeMethod.result(methodCode, f, frame);
        return result;
    }

    public static final String floatToRawIntBits = TYPE + "." + "floatToRawIntBits" + "(F)I";

    /**
     * native
     *
     * @see Float#floatToRawIntBits(float)
     */
    private static Result floatToRawIntBits(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        float value = (float) methodArgs.invokeArgs[0];
        int bits = Float.floatToRawIntBits(value);
        Result result = NativeMethod.result(methodCode, bits, frame);
        return result;
    }

    public static final String parseFloat = TYPE + "." + "parseFloat" + "(Ljava/lang/String;)F";

    /**
     * override
     *
     * @see Float#parseFloat(String)
     */
    private static Result parseFloat(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String s = frame.heap.runToString(objectRef);
        float f;
        try {
            f = Float.parseFloat(s);
        } catch (Exception e) {
            frame.throwNumberFormatException(e.getMessage());
            return null;
        }
        Result result = NativeMethod.result(methodCode, f, frame);
        return result;
    }

    public static final String toString = TYPE + "." + "toString" + "(F)Ljava/lang/String;";

    /**
     * override
     *
     * @see Float#toString(float)
     */
    private static Result toString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        float f = (float) methodArgs.invokeArgs[0];
        String s = Float.toString(f);
        ObjectRef ref = frame.heap.newString(s);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

    public static final String toHexString = TYPE + "." + "toHexString" + "(F)Ljava/lang/String;";

    /**
     * override
     *
     * @see Float#toHexString(float)
     */
    private static Result toHexString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        float f = (float) methodArgs.invokeArgs[0];
        String s = Float.toHexString(f);
        ObjectRef ref = frame.heap.newString(s);
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

}
