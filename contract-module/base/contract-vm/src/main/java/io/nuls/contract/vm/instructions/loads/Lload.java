package io.nuls.contract.vm.instructions.loads;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Lload {

    public static void lload(final Frame frame) {
        int index = frame.varInsnNode().var;
        long value = frame.localVariables.getLong(index);
        frame.operandStack.pushLong(value);

        //Log.result(frame.getCurrentOpCode(), value, index);
    }

}
