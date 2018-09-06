package io.nuls.contract.vm.instructions.stack;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Dup {

    public static void dup(final Frame frame) {
        Object value = frame.getOperandStack().pop();
        frame.getOperandStack().push(value);
        frame.getOperandStack().push(value);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup_x1(final Frame frame) {
        Object value1 = frame.getOperandStack().pop();
        Object value2 = frame.getOperandStack().pop();
        frame.getOperandStack().push(value1);
        frame.getOperandStack().push(value2);
        frame.getOperandStack().push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup_x2(final Frame frame) {
        Object value1 = frame.getOperandStack().pop();
        Object value2 = frame.getOperandStack().pop();
        Object value3 = frame.getOperandStack().pop();
        frame.getOperandStack().push(value1);
        frame.getOperandStack().push(value3);
        frame.getOperandStack().push(value2);
        frame.getOperandStack().push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup2(final Frame frame) {
        Object value1 = frame.getOperandStack().pop();
        Object value2 = frame.getOperandStack().pop();
        frame.getOperandStack().push(value2);
        frame.getOperandStack().push(value1);
        frame.getOperandStack().push(value2);
        frame.getOperandStack().push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup2_x1(final Frame frame) {
        Object value1 = frame.getOperandStack().pop();
        Object value2 = frame.getOperandStack().pop();
        Object value3 = frame.getOperandStack().pop();
        frame.getOperandStack().push(value2);
        frame.getOperandStack().push(value1);
        frame.getOperandStack().push(value3);
        frame.getOperandStack().push(value2);
        frame.getOperandStack().push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup2_x2(final Frame frame) {
        Object value1 = frame.getOperandStack().pop();
        Object value2 = frame.getOperandStack().pop();
        Object value3 = frame.getOperandStack().pop();
        Object value4 = frame.getOperandStack().pop();
        frame.getOperandStack().push(value2);
        frame.getOperandStack().push(value1);
        frame.getOperandStack().push(value4);
        frame.getOperandStack().push(value3);
        frame.getOperandStack().push(value2);
        frame.getOperandStack().push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
