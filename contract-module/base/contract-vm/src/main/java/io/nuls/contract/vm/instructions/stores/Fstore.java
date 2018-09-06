package io.nuls.contract.vm.instructions.stores;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Fstore {

    public static void fstore(final Frame frame) {
        int index = frame.varInsnNode().var;
        float value = frame.getOperandStack().popFloat();
        frame.getLocalVariables().setFloat(index, value);

        //Log.result(frame.getCurrentOpCode(), value, index);
    }

}
