package io.nuls.contract.vm.instructions.loads;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Aload {

    public static void aload(final Frame frame) {
        int index = frame.varInsnNode().var;
        ObjectRef objectRef = frame.getLocalVariables().getRef(index);
        frame.getOperandStack().pushRef(objectRef);

        //Log.result(frame.getCurrentOpCode(), objectRef, index);
    }

}
