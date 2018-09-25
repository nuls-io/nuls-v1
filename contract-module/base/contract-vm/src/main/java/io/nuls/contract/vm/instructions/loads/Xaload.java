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
package io.nuls.contract.vm.instructions.loads;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Xaload {

    public static void iaload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        int value = (int) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushInt(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void laload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        long value = (long) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushLong(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void faload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        float value = (float) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushFloat(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void daload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        double value = (double) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushDouble(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void aaload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        ObjectRef value = (ObjectRef) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushRef(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void baload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        Object result;
        if (arrayRef.getVariableType().getComponentType().isBoolean()) {
            boolean value = (boolean) frame.heap.getArray(arrayRef, index);
            frame.operandStack.pushBoolean(value);
            result = value;
        } else {
            byte value = (byte) frame.heap.getArray(arrayRef, index);
            frame.operandStack.pushByte(value);
            result = value;
        }

        //Log.result(frame.getCurrentOpCode(), result, arrayRef, index);
    }

    public static void caload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        char value = (char) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushChar(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void saload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        short value = (short) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushShort(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

}
