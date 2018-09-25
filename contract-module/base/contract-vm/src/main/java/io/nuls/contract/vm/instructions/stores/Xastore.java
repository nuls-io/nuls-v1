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
package io.nuls.contract.vm.instructions.stores;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Xastore {

    public static void iastore(final Frame frame) {
        int value = frame.operandStack.popInt();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void lastore(final Frame frame) {
        long value = frame.operandStack.popLong();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void fastore(final Frame frame) {
        float value = frame.operandStack.popFloat();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void dastore(final Frame frame) {
        double value = frame.operandStack.popDouble();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void aastore(final Frame frame) {
        ObjectRef value = frame.operandStack.popRef();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void bastore(final Frame frame) {
        int i = frame.operandStack.popInt();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        if (arrayRef.getVariableType().getComponentType().isBoolean()) {
            boolean value = i == 1 ? true : false;
            frame.heap.putArray(arrayRef, index, value);
            //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
        } else {
            byte value = (byte) i;
            frame.heap.putArray(arrayRef, index, value);
            //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
        }
    }

    public static void castore(final Frame frame) {
        char value = frame.operandStack.popChar();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void sastore(final Frame frame) {
        short value = frame.operandStack.popShort();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

}
