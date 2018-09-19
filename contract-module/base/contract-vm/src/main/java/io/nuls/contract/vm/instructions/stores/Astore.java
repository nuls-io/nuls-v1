package io.nuls.contract.vm.instructions.stores;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Astore {

    public static void astore(final Frame frame) {
        int index = frame.varInsnNode().var;
        ObjectRef objectRef = frame.operandStack.popRef();
        frame.localVariables.setRef(index, objectRef);

        //Log.result(frame.getCurrentOpCode(), objectRef, index);
    }

}
