package io.nuls.contract.vm.instructions.stores;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Istore {

    public static void istore(final Frame frame) {
        int index = frame.varInsnNode().var;
        int value = frame.operandStack.popInt();
        frame.localVariables.setInt(index, value);

        //Log.result(frame.getCurrentOpCode(), value, index);
    }

}
