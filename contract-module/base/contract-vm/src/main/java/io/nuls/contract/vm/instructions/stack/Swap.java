package io.nuls.contract.vm.instructions.stack;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Swap {

    public static void swap(final Frame frame) {
        Object value1 = frame.operandStack.pop();
        Object value2 = frame.operandStack.pop();
        frame.operandStack.push(value1);
        frame.operandStack.push(value2);

        //Log.opcode(frame.getCurrentOpCode(), value1, value2);
    }

}
