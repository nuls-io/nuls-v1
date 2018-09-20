/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.contract.vm.natives.java.sun.misc;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeUnsafe {

    public static final String TYPE = "sun/misc/Unsafe";

    public static boolean isSupport(MethodCode methodCode) {
        if (methodCode.isClass(TYPE) && (methodCode.isMethod("compareAndSwapLong", "(Ljava/lang/Object;JJJ)Z")
                || methodCode.isMethod("compareAndSwapObject", "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z")
        )) {
            return true;
        } else {
            return false;
        }
    }

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "compareAndSwapLong":
                result = compareAndSwapLong(methodCode, methodArgs, frame);
                break;
            case "compareAndSwapObject":
                result = compareAndSwapObject(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result compareAndSwapLong(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        int bits = (int) methodArgs.invokeArgs[0];
        float f = Float.intBitsToFloat(bits);
        Result result = NativeMethod.result(methodCode, f, frame);
        return result;
    }

    private static Result compareAndSwapObject(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        float value = (float) methodArgs.invokeArgs[0];
        int bits = Float.floatToRawIntBits(value);
        Result result = NativeMethod.result(methodCode, bits, frame);
        return result;
    }

}
