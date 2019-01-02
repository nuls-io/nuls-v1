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

public class LocalVariables {

    private int maxLocals;

    private Object[] localVariables;

    public LocalVariables(int maxLocals, Object[] args) {
        this.maxLocals = maxLocals;
        this.localVariables = new Object[maxLocals];
        if (args != null) {
            System.arraycopy(args, 0, this.localVariables, 0, args.length);
        }
    }

    public int getInt(int index) {
        Object object = this.localVariables[index];
        if (object instanceof Boolean) {
            return (boolean) object ? 1 : 0;
        } else if (object instanceof Byte) {
            return (byte) object;
        } else if (object instanceof Character) {
            return (char) object;
        } else if (object instanceof Short) {
            return (short) object;
        } else {
            return (int) object;
        }
    }

    public void setInt(int index, int value) {
        this.localVariables[index] = value;
    }

    public long getLong(int index) {
        return (long) this.localVariables[index];
    }

    public void setLong(int index, long value) {
        this.localVariables[index] = value;
    }

    public float getFloat(int index) {
        return (float) this.localVariables[index];
    }

    public void setFloat(int index, float value) {
        this.localVariables[index] = value;
    }

    public double getDouble(int index) {
        return (double) this.localVariables[index];
    }

    public void setDouble(int index, double value) {
        this.localVariables[index] = value;
    }

    public ObjectRef getRef(int index) {
        return (ObjectRef) this.localVariables[index];
    }

    public void setRef(int index, ObjectRef value) {
        this.localVariables[index] = value;
    }

}
