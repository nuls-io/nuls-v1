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
import io.nuls.contract.vm.util.CloneUtils;

import java.util.Map;

import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeObject {

    public static final String TYPE = "java/lang/Object";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case getClass:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getClass(methodCode, methodArgs, frame);
                }
            case hashCode:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return hashCode(methodCode, methodArgs, frame);
                }
            case clone:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return clone(methodCode, methodArgs, frame);
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

    public static final String getClass = TYPE + "." + "getClass" + "()Ljava/lang/Class;";

    /**
     * native
     *
     * @see Object#getClass()
     */
    private static Result getClass(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        ObjectRef classRef = frame.heap.getClassRef(objectRef.getVariableType().getDesc());
        Result result = NativeMethod.result(methodCode, classRef, frame);
        return result;
    }

    public static final String hashCode = TYPE + "." + "hashCode" + "()I";

    /**
     * native
     *
     * @see Object#hashCode()
     */
    private static Result hashCode(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        int hashCode = NativeSystem.identityHashCode(objectRef);
        Result result = NativeMethod.result(methodCode, hashCode, frame);
        return result;
    }

    public static final String clone = TYPE + "." + "clone" + "()Ljava/lang/Object;";

    /**
     * native
     *
     * @see Object#clone()
     */
    private static Result clone(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        ObjectRef newRef;
        if (objectRef.isArray()) {
            newRef = frame.heap.newArray(objectRef.getVariableType(), objectRef.getDimensions());
            frame.heap.arraycopy(objectRef, 0, newRef, 0, objectRef.getDimensions()[0]);
        } else {
            Map<String, Object> fields = frame.heap.getFields(objectRef);
            Map<String, Object> newFields = CloneUtils.clone(fields);
            newRef = frame.heap.newObjectRef(null, objectRef.getDesc(), objectRef.getDimensions());
            frame.heap.putFields(newRef, newFields);
        }
        Result result = NativeMethod.result(methodCode, newRef, frame);
        return result;
    }

}
