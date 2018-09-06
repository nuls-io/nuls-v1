package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.FieldInsnNode;

public class Putstatic {

    public static void putstatic(Frame frame) {
        FieldInsnNode fieldInsnNode = frame.fieldInsnNode();
        String className = fieldInsnNode.owner;
        String fieldName = fieldInsnNode.name;
        String fieldDesc = fieldInsnNode.desc;
        Object value;
        if ("J".equals(fieldDesc)) {
            value = frame.getOperandStack().popLong();
        } else if ("D".equals(fieldDesc)) {
            value = frame.getOperandStack().popDouble();
        } else {
            value = frame.getOperandStack().pop();
        }
        frame.getHeap().putStatic(className, fieldName, value);

        //Log.result(frame.getCurrentOpCode(), value, className, fieldName);
    }

}
