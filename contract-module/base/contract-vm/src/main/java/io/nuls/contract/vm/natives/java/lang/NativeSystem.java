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

public class NativeSystem {

    public static final String TYPE = "java/lang/System";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case arraycopy:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return arraycopy(methodCode, methodArgs, frame);
                }
            case identityHashCode:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return identityHashCode(methodCode, methodArgs, frame);
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

    public static final String arraycopy = TYPE + "." + "arraycopy" + "(Ljava/lang/Object;ILjava/lang/Object;II)V";

    /**
     * native
     *
     * @see System#arraycopy(Object, int, Object, int, int)
     */
    private static Result arraycopy(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Object[] args = methodArgs.invokeArgs;
        ObjectRef srcObjectRef = (ObjectRef) args[0];
        int srcPos = (int) args[1];
        ObjectRef destObjectRef = (ObjectRef) args[2];
        int destPos = (int) args[3];
        int length = (int) args[4];

        if (length > 0 && frame.checkArray(srcObjectRef, srcPos)
                && frame.checkArray(srcObjectRef, srcPos + length - 1)
                && frame.checkArray(destObjectRef, destPos)
                && frame.checkArray(destObjectRef, destPos + length - 1)) {
            frame.heap.arraycopy(srcObjectRef, srcPos, destObjectRef, destPos, length);
        }

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String identityHashCode = TYPE + "." + "identityHashCode" + "(Ljava/lang/Object;)I";

    /**
     * native
     *
     * @see System#identityHashCode(Object)
     */
    private static Result identityHashCode(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        int hashCode = identityHashCode(objectRef);
        Result result = NativeMethod.result(methodCode, hashCode, frame);
        return result;
    }

    public static int identityHashCode(ObjectRef objectRef) {
        int hashCode = 0;
        if (objectRef != null) {
            hashCode = objectRef.hashCode();
        }
        return hashCode;
    }

}
