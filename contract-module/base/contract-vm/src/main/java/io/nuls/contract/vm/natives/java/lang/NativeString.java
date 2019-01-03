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

public class NativeString {

    public static final String TYPE = "java/lang/String";

    public static Result override(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case getBytes:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getBytes(methodCode, methodArgs, frame);
                }
            default:
                return null;
        }
    }

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case intern:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return intern(methodCode, methodArgs, frame);
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

    public static final String getBytes = TYPE + "." + "getBytes" + "()[B";

    /**
     * override
     *
     * @see String#getBytes()
     */
    private static Result getBytes(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        ObjectRef ref = null;
        if (objectRef != null) {
            String str = frame.heap.runToString(objectRef);
            if (str != null) {
                byte[] bytes = str.getBytes();
                ref = frame.heap.newArray(bytes);
            }
        }
        Result result = NativeMethod.result(methodCode, ref, frame);
        return result;
    }

//    public static final String format = TYPE + "." + "format" + "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;";
//
//    /**
//     * override
//     *
//     * @see String#format(String, Object...)
//     */
//    private static Result format(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
//        Result result = NativeMethod.result(methodCode, null, frame);
//        return result;
//    }

    public static final String intern = TYPE + "." + "intern" + "()Ljava/lang/String;";

    /**
     * native
     *
     * @see String#intern()
     */
    private static Result intern(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

}
