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
package io.nuls.contract.vm.instructions.control;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.Descriptors;
import io.nuls.contract.vm.util.Log;

public class Return {

    public static void ireturn(final Frame frame) {
        Object result;
        switch (frame.result.getVariableType().getType()) {
            case Descriptors.BOOLEAN:
                result = frame.operandStack.popBoolean();
                break;
            case Descriptors.BYTE:
                result = frame.operandStack.popByte();
                break;
            case Descriptors.CHAR:
                result = frame.operandStack.popChar();
                break;
            case Descriptors.SHORT:
                result = frame.operandStack.popShort();
                break;
            default:
                result = frame.operandStack.popInt();
                break;
        }
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void lreturn(final Frame frame) {
        long result = frame.operandStack.popLong();
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void freturn(final Frame frame) {
        float result = frame.operandStack.popFloat();
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void dreturn(final Frame frame) {
        double result = frame.operandStack.popDouble();
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void areturn(final Frame frame) {
        ObjectRef result = frame.operandStack.popRef();
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void return_(final Frame frame) {
        frame.result.value(null);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
