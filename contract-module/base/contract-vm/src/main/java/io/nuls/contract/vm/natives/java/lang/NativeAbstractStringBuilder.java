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

import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeAbstractStringBuilder {

    public static final String TYPE = "java/lang/AbstractStringBuilder";

    public static final String appendD = TYPE + "." + "append" + "(D)Ljava/lang/AbstractStringBuilder;";

    public static final String appendF = TYPE + "." + "append" + "(F)Ljava/lang/AbstractStringBuilder;";

    public static Result override(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case appendD:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return append(methodCode, methodArgs, frame);
                }
            case appendF:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return append(methodCode, methodArgs, frame);
                }
            default:
                return null;
        }
    }

    /**
     * override
     *
     * @see AbstractStringBuilder#append(float)
     * @see AbstractStringBuilder#append(double)
     */
    private static Result append(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        Object a = methodArgs.invokeArgs[0];
        ObjectRef ref = frame.heap.newString(a.toString());
        MethodCode append = frame.methodArea.loadMethod(TYPE, "append", "(Ljava/lang/String;)Ljava/lang/AbstractStringBuilder;");
        frame.vm.run(append, new Object[]{objectRef, ref}, false);
        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

}
