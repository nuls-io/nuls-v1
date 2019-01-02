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
package io.nuls.contract.vm.code;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

public class LocalVariableCode {

    /**
     * The name of a local variable.
     */
    public final String name;

    /**
     * The type descriptor of this local variable.
     */
    public final String desc;

    /**
     * The signature of this local variable. May be <tt>null</tt>.
     */
    public final String signature;

    /**
     * The first instruction corresponding to the scope of this local variable (inclusive).
     */
    public final LabelNode start;

    /**
     * The last instruction corresponding to the scope of this local variable (exclusive).
     */
    public final LabelNode end;

    /**
     * The local variable's index.
     */
    public final int index;

    public final VariableType variableType;

    public LocalVariableCode(LocalVariableNode localVariableNode) {
        name = localVariableNode.name;
        desc = localVariableNode.desc;
        signature = localVariableNode.signature;
        start = localVariableNode.start;
        end = localVariableNode.end;
        index = localVariableNode.index;
        //
        variableType = VariableType.valueOf(desc);
    }

}
