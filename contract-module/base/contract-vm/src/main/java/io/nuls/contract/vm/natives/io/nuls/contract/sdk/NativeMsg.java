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
package io.nuls.contract.vm.natives.io.nuls.contract.sdk;

import io.nuls.contract.sdk.Msg;
import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

import static io.nuls.contract.vm.natives.NativeMethod.SUCCESS;

public class NativeMsg {

    public static final String TYPE = "io/nuls/contract/sdk/Msg";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case gasleft:
                if (check) {
                    return SUCCESS;
                } else {
                    return gasleft(methodCode, methodArgs, frame);
                }
            case sender:
                if (check) {
                    return SUCCESS;
                } else {
                    return sender(methodCode, methodArgs, frame);
                }
            case value:
                if (check) {
                    return SUCCESS;
                } else {
                    return value(methodCode, methodArgs, frame);
                }
            case gasprice:
                if (check) {
                    return SUCCESS;
                } else {
                    return gasprice(methodCode, methodArgs, frame);
                }
            case address:
                if (check) {
                    return SUCCESS;
                } else {
                    return address(methodCode, methodArgs, frame);
                }
            default:
                frame.nonsupportMethod(methodCode);
                return null;
        }
    }

    public static final String gasleft = TYPE + "." + "gasleft" + "()J";

    /**
     * native
     *
     * @see Msg#gasleft()
     */
    private static Result gasleft(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getGasLeft(), frame);
        return result;
    }

    public static final String sender = TYPE + "." + "sender" + "()Lio/nuls/contract/sdk/Address;";

    /**
     * native
     *
     * @see Msg#sender()
     */
    private static Result sender(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getProgramContext().getSender(), frame);
        return result;
    }

    public static final String value = TYPE + "." + "value" + "()Ljava/math/BigInteger;";

    /**
     * native
     *
     * @see Msg#value()
     */
    private static Result value(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getProgramContext().getValue(), frame);
        return result;
    }

    public static final String gasprice = TYPE + "." + "gasprice" + "()J";

    /**
     * native
     *
     * @see Msg#gasprice()
     */
    private static Result gasprice(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getProgramContext().getGasPrice(), frame);
        return result;
    }

    public static final String address = TYPE + "." + "address" + "()Lio/nuls/contract/sdk/Address;";

    /**
     * native
     *
     * @see Msg#address()
     */
    private static Result address(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getProgramContext().getAddress(), frame);
        return result;
    }

}
