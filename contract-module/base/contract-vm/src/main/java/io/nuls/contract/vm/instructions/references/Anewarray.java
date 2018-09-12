package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Constants;
import org.objectweb.asm.tree.TypeInsnNode;

public class Anewarray {

    public static void anewarray(Frame frame) {
        TypeInsnNode typeInsnNode = frame.typeInsnNode();
        String className = typeInsnNode.desc;
        int length = frame.operandStack.popInt();
        if (length < 0) {
            frame.throwNegativeArraySizeException();
            return;
        } else {
            ObjectRef arrayRef;
            if (className.contains(Constants.ARRAY_START)) {
                VariableType type = VariableType.valueOf(className);
                int[] dimensions = new int[type.getDimensions() + 1];
                dimensions[0] = length;
                VariableType variableType = VariableType.valueOf(Constants.ARRAY_START + className);
                arrayRef = frame.heap.newArray(variableType, dimensions);
            } else {
                VariableType variableType = VariableType.valueOf(Constants.ARRAY_PREFIX + className + Constants.ARRAY_SUFFIX);
                arrayRef = frame.heap.newArray(variableType, length);
            }
            frame.operandStack.pushRef(arrayRef);

            //Log.opcode(frame.getCurrentOpCode(), arrayRef);
        }
    }

}
