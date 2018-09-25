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
package io.nuls.contract.vm.instructions.stack;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Dup {

    public static void dup(final Frame frame) {
        Object value = frame.operandStack.pop();
        frame.operandStack.push(value);
        frame.operandStack.push(value);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup_x1(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        frame.operandStack.push(value1);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup_x2(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        Object value3 = frame.operandStack.pop();
        frame.operandStack.push(value1);
        frame.operandStack.push(value3);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup2(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup2_x1(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        Object value3 = frame.operandStack.pop();
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);
        frame.operandStack.push(value3);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup2_x2(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        Object value3 = frame.operandStack.pop();
        Object value4 = frame.operandStack.pop();
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);
        frame.operandStack.push(value4);
        frame.operandStack.push(value3);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
