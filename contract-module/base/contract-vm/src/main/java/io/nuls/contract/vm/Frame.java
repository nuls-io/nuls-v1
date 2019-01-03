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

import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.instructions.references.Athrow;
import org.objectweb.asm.tree.*;

public class Frame {

    public final VM vm;

    public final Heap heap;

    public final MethodArea methodArea;

    public final MethodCode methodCode;

    public final int maxStack;

    public final int maxLocals;

    public final OperandStack operandStack;

    public final LocalVariables localVariables;

    public final Result result;

    private AbstractInsnNode currentInsnNode;

    private OpCode currentOpCode;

    public boolean addGas = true;

    public Frame(VM vm, MethodCode methodCode, Object[] args) {
        this.vm = vm;
        this.heap = vm.heap;
        this.methodArea = vm.methodArea;
        this.methodCode = methodCode;
        this.maxStack = this.methodCode.maxStack;
        this.maxLocals = this.methodCode.maxLocals;
        this.operandStack = new OperandStack(this.maxStack);
        this.localVariables = new LocalVariables(this.maxLocals, args);
        this.result = new Result(this.methodCode.returnVariableType);
        this.currentInsnNode = this.methodCode.instructions.getFirst();
    }

    public void step() {
        if (this.currentInsnNode != null) {
            this.currentInsnNode = this.currentInsnNode.getNext();
        }
    }

    public void jump() {
        this.currentInsnNode = jumpInsnNode().label;
    }

    public void jump(LabelNode label) {
        this.currentInsnNode = label;
    }

    public OpCode currentOpCode() {
        if (this.currentInsnNode != null) {
            this.currentOpCode = OpCode.valueOf(this.currentInsnNode.getOpcode());
        } else {
            this.currentOpCode = null;
        }
        return this.currentOpCode;
    }

    public int getLine(LabelNode labelNode) {
        AbstractInsnNode abstractInsnNode = labelNode;
        while (!(abstractInsnNode instanceof LineNumberNode)) {
            abstractInsnNode = abstractInsnNode.getNext();
        }
        return ((LineNumberNode) abstractInsnNode).line;
    }

    public int getLine() {
        AbstractInsnNode abstractInsnNode = this.currentInsnNode;
        while (!(abstractInsnNode instanceof LineNumberNode)) {
            abstractInsnNode = abstractInsnNode.getPrevious();
        }
        return ((LineNumberNode) abstractInsnNode).line;
    }

    public boolean checkArray(ObjectRef arrayRef, int index) {
        if (arrayRef == null) {
            throwNullPointerException();
            return false;
        }
        int length = arrayRef.getDimensions()[0];
        if (index < 0 || index >= length) {
            throwArrayIndexOutOfBoundsException(index);
            return false;
        }
        return true;
    }

    private void throwException(ObjectRef objectRef) {
        this.operandStack.pushRef(objectRef);
        Athrow.athrow(this);
    }

    public void throwRuntimeException(String message) {
        ObjectRef objectRef = this.heap.runNewObject(VariableType.RUNTIME_EXCEPTION_TYPE, message);
        throwException(objectRef);
    }

    public void throwNumberFormatException(String message) {
        ObjectRef objectRef = this.heap.runNewObject(VariableType.NUMBER_FORMAT_EXCEPTION_TYPE, message);
        throwException(objectRef);
    }

    public void throwNullPointerException() {
        ObjectRef objectRef = this.heap.runNewObject(VariableType.NULL_POINTER_EXCEPTION_TYPE);
        throwException(objectRef);
    }

    public void throwArrayIndexOutOfBoundsException(int index) {
        ObjectRef objectRef = this.heap.runNewObject(VariableType.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION_TYPE);
        throwException(objectRef);
    }

    public void throwNegativeArraySizeException() {
        ObjectRef objectRef = this.heap.runNewObject(VariableType.NEGATIVE_ARRAY_SIZE_EXCEPTION_TYPE);
        throwException(objectRef);
    }

    public void throwClassCastException() {
        ObjectRef objectRef = this.heap.runNewObject(VariableType.CLASS_CAST_EXCEPTION_TYPE);
        throwException(objectRef);
    }

    private void throwError(ObjectRef objectRef) {
        this.vm.getResult().error(objectRef);
    }

    public void throwStackOverflowError() {
        ObjectRef objectRef = this.heap.runNewObject(VariableType.STACK_OVERFLOW_ERROR_TYPE);
        throwError(objectRef);
    }

    public void nonsupportOpCode() {
        int line = getLine();
        throw new RuntimeException(String.format("nonsupport opcode：class(%s), line(%d)", methodCode.className, line));
    }

    public void nonsupportMethod(MethodCode methodCode) {
        throw new RuntimeException("nonsupport method: " + methodCode.fullName);
    }

    public InsnNode insnNode() {
        return (InsnNode) this.currentInsnNode;
    }

    public IntInsnNode intInsnNode() {
        return (IntInsnNode) this.currentInsnNode;
    }

    public VarInsnNode varInsnNode() {
        return (VarInsnNode) this.currentInsnNode;
    }

    public TypeInsnNode typeInsnNode() {
        return (TypeInsnNode) this.currentInsnNode;
    }

    public FieldInsnNode fieldInsnNode() {
        return (FieldInsnNode) this.currentInsnNode;
    }

    public MethodInsnNode methodInsnNode() {
        return (MethodInsnNode) this.currentInsnNode;
    }

    public InvokeDynamicInsnNode invokeDynamicInsnNode() {
        return (InvokeDynamicInsnNode) this.currentInsnNode;
    }

    public JumpInsnNode jumpInsnNode() {
        return (JumpInsnNode) this.currentInsnNode;
    }

    public LabelNode labelNode() {
        return (LabelNode) this.currentInsnNode;
    }

    public LdcInsnNode ldcInsnNode() {
        return (LdcInsnNode) this.currentInsnNode;
    }

    public IincInsnNode iincInsnNode() {
        return (IincInsnNode) this.currentInsnNode;
    }

    public TableSwitchInsnNode tableSwitchInsnNode() {
        return (TableSwitchInsnNode) this.currentInsnNode;
    }

    public LookupSwitchInsnNode lookupSwitchInsnNode() {
        return (LookupSwitchInsnNode) this.currentInsnNode;
    }

    public MultiANewArrayInsnNode multiANewArrayInsnNode() {
        return (MultiANewArrayInsnNode) this.currentInsnNode;
    }

    public FrameNode frameNode() {
        return (FrameNode) this.currentInsnNode;
    }

    public LineNumberNode lineNumberNode() {
        return (LineNumberNode) this.currentInsnNode;
    }

    public AbstractInsnNode getCurrentInsnNode() {
        return currentInsnNode;
    }

    public void setAddGas(boolean addGas) {
        this.addGas = addGas;
    }

    public OpCode getCurrentOpCode() {
        return currentOpCode;
    }

}
