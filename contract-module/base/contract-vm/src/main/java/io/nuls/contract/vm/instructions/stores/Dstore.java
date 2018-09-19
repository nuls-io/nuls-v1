package io.nuls.contract.vm.instructions.stores;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Dstore {

    public static void dstore(final Frame frame) {
        int index = frame.varInsnNode().var;
        double value = frame.operandStack.popDouble();
        frame.localVariables.setDouble(index, value);

        //Log.result(frame.getCurrentOpCode(), value, index);
    }

}
