package io.nuls.contract.vm.instructions.stack;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Dup {

    public static void dup(final Frame frame) {
        Object value = frame.operandStack.pop();
        frame.operandStack.push(value);
        frame.operandStack.push(value);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup_x1(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        frame.operandStack.push(value1);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup_x2(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        Object value3 = frame.operandStack.pop();
        frame.operandStack.push(value1);
        frame.operandStack.push(value3);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup2(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup2_x1(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        Object value3 = frame.operandStack.pop();
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);
        frame.operandStack.push(value3);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

    public static void dup2_x2(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        Object value3 = frame.operandStack.pop();
        Object value4 = frame.operandStack.pop();
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);
        frame.operandStack.push(value4);
        frame.operandStack.push(value3);
        frame.operandStack.push(value2);
        frame.operandStack.push(value1);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
