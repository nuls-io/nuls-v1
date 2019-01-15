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
package io.nuls.contract.vm;

import io.nuls.contract.vm.code.VariableType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodArgs {

    public final Object[] frameArgs;

    public final Object[] invokeArgs;

    public final ObjectRef objectRef;

    public MethodArgs(List<VariableType> argsVariableType, OperandStack operandStack, boolean isStatic) {
        int size = argsVariableType.size();
        List frameList = new ArrayList();
        List invokeList = new ArrayList();
        for (int i = size - 1; i >= 0; i--) {
            VariableType variableType = argsVariableType.get(i);
            if (variableType.isLong() || variableType.isDouble()) {
                frameList.add(operandStack.pop());
            }
            Object value = operandStack.pop();
            frameList.add(value);
            invokeList.add(variableType.getPrimitiveValue(value));
        }
        if (!isStatic) {
            this.objectRef = (ObjectRef) operandStack.pop();
            frameList.add(this.objectRef);
        } else {
            this.objectRef = null;
        }
        Collections.reverse(frameList);
        Collections.reverse(invokeList);
        this.frameArgs = frameList.toArray();
        this.invokeArgs = invokeList.toArray();
    }

}
