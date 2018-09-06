package io.nuls.contract.vm.instructions.loads;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Iload {

    public static void iload(final Frame frame) {
        int index = frame.varInsnNode().var;
        int value = frame.getLocalVariables().getInt(index);
        frame.getOperandStack().pushInt(value);

        //Log.result(frame.getCurrentOpCode(), value, index);
    }

}
