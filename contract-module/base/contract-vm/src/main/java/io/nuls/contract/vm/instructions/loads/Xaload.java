package io.nuls.contract.vm.instructions.loads;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Xaload {

    public static void iaload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        int value = (int) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushInt(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void laload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        long value = (long) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushLong(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void faload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        float value = (float) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushFloat(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void daload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        double value = (double) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushDouble(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void aaload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        ObjectRef value = (ObjectRef) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushRef(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void baload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        Object result;
        if (arrayRef.getVariableType().getComponentType().isBoolean()) {
            boolean value = (boolean) frame.heap.getArray(arrayRef, index);
            frame.operandStack.pushBoolean(value);
            result = value;
        } else {
            byte value = (byte) frame.heap.getArray(arrayRef, index);
            frame.operandStack.pushByte(value);
            result = value;
        }

        //Log.result(frame.getCurrentOpCode(), result, arrayRef, index);
    }

    public static void caload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        char value = (char) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushChar(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void saload(final Frame frame) {
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        short value = (short) frame.heap.getArray(arrayRef, index);
        frame.operandStack.pushShort(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

}
