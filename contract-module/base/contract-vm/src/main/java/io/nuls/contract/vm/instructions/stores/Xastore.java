package io.nuls.contract.vm.instructions.stores;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Xastore {

    public static void iastore(final Frame frame) {
        int value = frame.operandStack.popInt();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void lastore(final Frame frame) {
        long value = frame.operandStack.popLong();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void fastore(final Frame frame) {
        float value = frame.operandStack.popFloat();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void dastore(final Frame frame) {
        double value = frame.operandStack.popDouble();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void aastore(final Frame frame) {
        ObjectRef value = frame.operandStack.popRef();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void bastore(final Frame frame) {
        int i = frame.operandStack.popInt();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        if (arrayRef.getVariableType().getComponentType().isBoolean()) {
            boolean value = i == 1 ? true : false;
            frame.heap.putArray(arrayRef, index, value);
            //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
        } else {
            byte value = (byte) i;
            frame.heap.putArray(arrayRef, index, value);
            //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
        }
    }

    public static void castore(final Frame frame) {
        char value = frame.operandStack.popChar();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

    public static void sastore(final Frame frame) {
        short value = frame.operandStack.popShort();
        int index = frame.operandStack.popInt();
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (!frame.checkArray(arrayRef, index)) {
            return;
        }
        frame.heap.putArray(arrayRef, index, value);

        //Log.result(frame.getCurrentOpCode(), arrayRef, index, value);
    }

}
