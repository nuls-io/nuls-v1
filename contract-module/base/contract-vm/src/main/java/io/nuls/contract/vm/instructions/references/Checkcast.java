package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.TypeInsnNode;

public class Checkcast {

    public static void checkcast(Frame frame) {
        TypeInsnNode typeInsnNode = frame.typeInsnNode();
        String desc = typeInsnNode.desc;
        VariableType variableType = VariableType.valueOf(desc);
        ObjectRef objectRef = frame.operandStack.popRef();

        if (objectRef == null || Instanceof.instanceof_(objectRef, variableType, frame)) {
            frame.operandStack.pushRef(objectRef);
        } else {
            frame.throwClassCastException();
        }

        //Log.opcode(frame.getCurrentOpCode(), objectRef, desc);
    }

}
