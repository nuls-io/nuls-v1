package io.nuls.contract.vm.instructions.extended;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

public class Multianewarray {

    public static void multianewarray(final Frame frame) {
        MultiANewArrayInsnNode multiANewArrayInsnNode = frame.multiANewArrayInsnNode();
        int[] dimensions = new int[multiANewArrayInsnNode.dims];
        for (int i = multiANewArrayInsnNode.dims - 1; i >= 0; i--) {
            int length = frame.getOperandStack().popInt();
            if (length < 0) {
                frame.throwNegativeArraySizeException();
                return;
            }
            dimensions[i] = length;
        }
        VariableType variableType = VariableType.valueOf(multiANewArrayInsnNode.desc);
        ObjectRef arrayRef = frame.getHeap().newArray(variableType, dimensions);
        frame.getOperandStack().pushRef(arrayRef);

        //Log.result(frame.getCurrentOpCode(), arrayRef);
    }

}

