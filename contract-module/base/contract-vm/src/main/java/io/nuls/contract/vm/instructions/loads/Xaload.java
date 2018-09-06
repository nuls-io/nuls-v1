package io.nuls.contract.vm.instructions.loads;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Xaload {

    public static void iaload(final Frame frame) {
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        int value = (int) frame.getHeap().getArray(arrayRef, index);
        frame.getOperandStack().pushInt(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void laload(final Frame frame) {
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        long value = (long) frame.getHeap().getArray(arrayRef, index);
        frame.getOperandStack().pushLong(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void faload(final Frame frame) {
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        float value = (float) frame.getHeap().getArray(arrayRef, index);
        frame.getOperandStack().pushFloat(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void daload(final Frame frame) {
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        double value = (double) frame.getHeap().getArray(arrayRef, index);
        frame.getOperandStack().pushDouble(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void aaload(final Frame frame) {
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        ObjectRef value = (ObjectRef) frame.getHeap().getArray(arrayRef, index);
        frame.getOperandStack().pushRef(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void baload(final Frame frame) {
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        Object result;
        if (arrayRef.getVariableType().getComponentType().isBoolean()) {
            boolean value = (boolean) frame.getHeap().getArray(arrayRef, index);
            frame.getOperandStack().pushBoolean(value);
            result = value;
        } else {
            byte value = (byte) frame.getHeap().getArray(arrayRef, index);
            frame.getOperandStack().pushByte(value);
            result = value;
        }

        //Log.result(frame.getCurrentOpCode(), result, arrayRef, index);
    }

    public static void caload(final Frame frame) {
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        char value = (char) frame.getHeap().getArray(arrayRef, index);
        frame.getOperandStack().pushChar(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

    public static void saload(final Frame frame) {
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        short value = (short) frame.getHeap().getArray(arrayRef, index);
        frame.getOperandStack().pushShort(value);

        //Log.result(frame.getCurrentOpCode(), value, arrayRef, index);
    }

}
