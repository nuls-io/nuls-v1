package io.nuls.contract.vm.instructions.stores;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Lstore {

    public static void lstore(final Frame frame) {
        int index = frame.varInsnNode().var;
        long value = frame.operandStack.popLong();
        frame.localVariables.setLong(index, value);

        //Log.result(frame.getCurrentOpCode(), value, index);
    }

}
