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

import io.nuls.contract.vm.code.Descriptors;
import io.nuls.contract.vm.code.VariableType;

import java.util.Stack;

public class OperandStack extends Stack {

    private final int maxStack;

    public OperandStack(int maxStack) {
        super();
        this.maxStack = maxStack;
    }

    @Override
    public Object push(Object value) {
        return super.push(value);
    }

    public Object push(Object value, VariableType variableType) {
        if (variableType.isPrimitive()) {
            switch (variableType.getType()) {
                case Descriptors.INT:
                    value = pushInt((int) value);
                    break;
                case Descriptors.LONG:
                    value = pushLong((long) value);
                    break;
                case Descriptors.FLOAT:
                    value = pushFloat((float) value);
                    break;
                case Descriptors.DOUBLE:
                    value = pushDouble((double) value);
                    break;
                case Descriptors.BOOLEAN:
                    value = pushBoolean((boolean) value);
                    break;
                case Descriptors.BYTE:
                    value = pushByte((byte) value);
                    break;
                case Descriptors.CHAR:
                    value = pushChar((char) value);
                    break;
                case Descriptors.SHORT:
                    value = pushShort((short) value);
                    break;
                default:
                    value = push(value);
                    break;
            }
        } else {
            value = push(value);
        }
        return value;
    }

    @Override
    public synchronized Object pop() {
        return super.pop();
    }

    public int pushInt(int value) {
        push(value);
        return value;
    }

    public int popInt() {
        return (int) pop();
    }

    public long pushLong(long value) {
        push(value);
        push(null);
        return value;
    }

    public long popLong() {
        pop();
        return (long) pop();
    }

    public float pushFloat(float value) {
        push(value);
        return value;
    }

    public float popFloat() {
        return (float) pop();
    }

    public double pushDouble(double value) {
        push(value);
        push(null);
        return value;
    }

    public double popDouble() {
        pop();
        return (double) pop();
    }

    public int pushBoolean(boolean value) {
        return pushInt(value ? 1 : 0);
    }

    public boolean popBoolean() {
        return popInt() == 1 ? true : false;
    }

    public int pushByte(byte value) {
        return pushInt(value);
    }

    public byte popByte() {
        return (byte) popInt();
    }

    public int pushChar(char value) {
        return pushInt(value);
    }

    public char popChar() {
        return (char) popInt();
    }

    public int pushShort(short value) {
        return pushInt(value);
    }

    public short popShort() {
        return (short) popInt();
    }

    public ObjectRef pushRef(ObjectRef ref) {
        push(ref);
        return ref;
    }

    public ObjectRef popRef() {
        return (ObjectRef) pop();
    }

}
