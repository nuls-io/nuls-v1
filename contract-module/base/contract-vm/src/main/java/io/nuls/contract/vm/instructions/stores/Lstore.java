package io.nuls.contract.vm.instructions.stores;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Lstore {

    public static void lstore(final Frame frame) {
        int index = frame.varInsnNode().var;
        long value = frame.getOperandStack().popLong();
        frame.getLocalVariables().setLong(index, value);

        //Log.result(frame.getCurrentOpCode(), value, index);
    }

}
