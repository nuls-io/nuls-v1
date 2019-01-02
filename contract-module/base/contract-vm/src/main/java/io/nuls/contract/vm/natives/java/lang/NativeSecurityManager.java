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

import java.security.Permission;

import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeSecurityManager {

    public static final String TYPE = "java/lang/SecurityManager";

    public static Result override(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case getRootGroup:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getRootGroup(methodCode, methodArgs, frame);
                }
            case checkAccessThread:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return checkAccessThread(methodCode, methodArgs, frame);
                }
            case checkAccess:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return checkAccess(methodCode, methodArgs, frame);
                }
            case checkPropertyAccess:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return checkPropertyAccess(methodCode, methodArgs, frame);
                }
            case checkPermission:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return checkPermission(methodCode, methodArgs, frame);
                }
            default:
                return null;
        }
    }

    public static final String getRootGroup = TYPE + "." + "getRootGroup" + "()Ljava/lang/ThreadGroup;";

    /**
     * override
     *
     * @see SecurityManager#getRootGroup()
     */
    private static Result getRootGroup(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String checkAccessThread = TYPE + "." + "checkAccess" + "(Ljava/lang/Thread;)V";

    /**
     * override
     *
     * @see SecurityManager#checkAccess(Thread)
     */
    private static Result checkAccessThread(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String checkAccess = TYPE + "." + "checkAccess" + "(Ljava/lang/ThreadGroup;)V";

    /**
     * override
     *
     * @see SecurityManager#checkAccess(ThreadGroup)
     */
    private static Result checkAccess(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String checkPropertyAccess = TYPE + "." + "checkPropertyAccess" + "(Ljava/lang/String;)V";

    /**
     * override
     *
     * @see SecurityManager#checkPropertyAccess(String)
     */
    private static Result checkPropertyAccess(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String checkPermission = TYPE + "." + "checkPermission" + "(Ljava/security/Permission;)V";

    /**
     * override
     *
     * @see SecurityManager#checkPermission(Permission)
     */
    private static Result checkPermission(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

}
