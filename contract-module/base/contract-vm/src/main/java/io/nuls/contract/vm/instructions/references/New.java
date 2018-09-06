package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class New {

    public static void new_(Frame frame) {
        String className = frame.typeInsnNode().desc;
        ObjectRef objectRef = frame.getHeap().newObject(className);
        frame.getOperandStack().pushRef(objectRef);

        //Log.opcode(frame.getCurrentOpCode(), objectRef);
    }

}
