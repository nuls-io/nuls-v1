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
package io.nuls.contract.vm.instructions.extended;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

public class Multianewarray {

    public static void multianewarray(final Frame frame) {
        MultiANewArrayInsnNode multiANewArrayInsnNode = frame.multiANewArrayInsnNode();
        int[] dimensions = new int[multiANewArrayInsnNode.dims];
        for (int i = multiANewArrayInsnNode.dims - 1; i >= 0; i--) {
            int length = frame.operandStack.popInt();
            if (length < 0) {
                frame.throwNegativeArraySizeException();
                return;
            }
            dimensions[i] = length;
        }
        VariableType variableType = VariableType.valueOf(multiANewArrayInsnNode.desc);
        ObjectRef arrayRef = frame.heap.newArray(variableType, dimensions);
        frame.operandStack.pushRef(arrayRef);

        //Log.result(frame.getCurrentOpCode(), arrayRef);
    }

}

