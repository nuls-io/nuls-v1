package io.nuls.contract.vm.instructions.loads;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Fload {

    public static void fload(final Frame frame) {
        int index = frame.varInsnNode().var;
        float value = frame.localVariables.getFloat(index);
        frame.operandStack.pushFloat(value);

        //Log.result(frame.getCurrentOpCode(), value, index);
    }

}
