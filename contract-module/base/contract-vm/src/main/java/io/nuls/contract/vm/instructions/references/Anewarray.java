package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.TypeInsnNode;

public class Anewarray {

    public static void anewarray(Frame frame) {
        TypeInsnNode typeInsnNode = frame.typeInsnNode();
        String className = typeInsnNode.desc;
        int length = frame.getOperandStack().popInt();
        if (length < 0) {
            frame.throwNegativeArraySizeException();
            return;
        } else {
            VariableType variableType = VariableType.valueOf("[L" + className + ";");
            ObjectRef arrayRef = frame.getHeap().newArray(variableType, length);
            frame.getOperandStack().pushRef(arrayRef);
        }

        //Log.opcode(frame.getCurrentOpCode());
    }

}
