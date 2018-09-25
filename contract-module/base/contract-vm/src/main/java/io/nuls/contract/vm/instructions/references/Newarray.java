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
package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.Opcodes;

public class Newarray {

    public static void newarray(Frame frame) {
        VariableType type;
        int length = frame.operandStack.popInt();
        if (length < 0) {
            frame.throwNegativeArraySizeException();
            return;
        } else {
            switch (frame.intInsnNode().operand) {
                case Opcodes.T_BOOLEAN:
                    type = VariableType.BOOLEAN_ARRAY_TYPE;
                    break;
                case Opcodes.T_CHAR:
                    type = VariableType.CHAR_ARRAY_TYPE;
                    break;
                case Opcodes.T_FLOAT:
                    type = VariableType.FLOAT_ARRAY_TYPE;
                    break;
                case Opcodes.T_DOUBLE:
                    type = VariableType.DOUBLE_ARRAY_TYPE;
                    break;
                case Opcodes.T_BYTE:
                    type = VariableType.BYTE_ARRAY_TYPE;
                    break;
                case Opcodes.T_SHORT:
                    type = VariableType.SHORT_ARRAY_TYPE;
                    break;
                case Opcodes.T_INT:
                    type = VariableType.INT_ARRAY_TYPE;
                    break;
                case Opcodes.T_LONG:
                    type = VariableType.LONG_ARRAY_TYPE;
                    break;
                default:
                    throw new IllegalArgumentException("unknown operand");
            }
        }

        ObjectRef arrayRef = frame.heap.newArray(type, length);
        frame.operandStack.pushRef(arrayRef);

        //Log.result(frame.getCurrentOpCode(), arrayRef);
    }

}
