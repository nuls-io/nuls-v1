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
package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.TypeInsnNode;

public class Checkcast {

    public static void checkcast(Frame frame) {
        TypeInsnNode typeInsnNode = frame.typeInsnNode();
        String desc = typeInsnNode.desc;
        VariableType variableType = VariableType.valueOf(desc);
        ObjectRef objectRef = frame.operandStack.popRef();

        if (objectRef == null || Instanceof.instanceof_(objectRef, variableType, frame)) {
            frame.operandStack.pushRef(objectRef);
        } else {
            frame.throwClassCastException();
        }

        //Log.opcode(frame.getCurrentOpCode(), objectRef, desc);
    }

}
