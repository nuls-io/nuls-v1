package io.nuls.contract.vm.instructions.loads;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Dload {

    public static void dload(final Frame frame) {
        int index = frame.varInsnNode().var;
        double value = frame.getLocalVariables().getDouble(index);
        frame.getOperandStack().pushDouble(value);

        //Log.result(frame.getCurrentOpCode(), value, index);
    }

}
