package io.nuls.contract.vm.instructions.stores;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Xastore {

    public static void iastore(final Frame frame) {
        int value = frame.getOperandStack().popInt();
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.getHeap().putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void lastore(final Frame frame) {
        long value = frame.getOperandStack().popLong();
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.getHeap().putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void fastore(final Frame frame) {
        float value = frame.getOperandStack().popFloat();
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.getHeap().putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void dastore(final Frame frame) {
        double value = frame.getOperandStack().popDouble();
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.getHeap().putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void aastore(final Frame frame) {
        ObjectRef value = frame.getOperandStack().popRef();
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.getHeap().putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void bastore(final Frame frame) {
        int i = frame.getOperandStack().popInt();
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        if (arrayRef.getVariableType().getComponentType().isBoolean()) {
            boolean value = i == 1 ? true : false;
            frame.getHeap().putArray(arrayRef, index, value);
            //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
        } else {
            byte value = (byte) i;
            frame.getHeap().putArray(arrayRef, index, value);
            //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
        }
    }

    public static void castore(final Frame frame) {
        char value = frame.getOperandStack().popChar();
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.getHeap().putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void sastore(final Frame frame) {
        short value = frame.getOperandStack().popShort();
        int index = frame.getOperandStack().popInt();
        ObjectRef arrayRef = frame.getOperandStack().popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.getHeap().putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

}
