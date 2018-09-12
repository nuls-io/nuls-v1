package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.code.Descriptors;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.FieldInsnNode;

public class Putstatic {

    public static void putstatic(Frame frame) {
        FieldInsnNode fieldInsnNode = frame.fieldInsnNode();
        String className = fieldInsnNode.owner;
        String fieldName = fieldInsnNode.name;
        String fieldDesc = fieldInsnNode.desc;
        Object value;
        if (Descriptors.LONG_DESC.equals(fieldDesc)) {
            value = frame.operandStack.popLong();
        } else if (Descriptors.DOUBLE_DESC.equals(fieldDesc)) {
            value = frame.operandStack.popDouble();
        } else {
            value = frame.operandStack.pop();
        }
        frame.heap.putStatic(className, fieldName, value);

        //Log.result(frame.getCurrentOpCode(), value, className, fieldName);
    }

}
