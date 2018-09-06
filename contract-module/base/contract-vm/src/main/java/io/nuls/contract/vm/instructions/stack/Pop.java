package io.nuls.contract.vm.instructions.stack;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Pop {

    public static void pop(final Frame frame) {
        Object value = frame.getOperandStack().pop();

        //Log.opcode(frame.getCurrentOpCode(), value);
    }

    public static void pop2(final Frame frame) {
        Object value1 = frame.getOperandStack().pop();
        Object value2 = frame.getOperandStack().pop();

        //Log.opcode(frame.getCurrentOpCode(), value1, value2);
    }

}
