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
