package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.FieldInsnNode;

public class Getstatic {

    public static void getstatic(Frame frame) {
        FieldInsnNode fieldInsnNode = frame.fieldInsnNode();
        String className = fieldInsnNode.owner;
        String fieldName = fieldInsnNode.name;
        String fieldDesc = fieldInsnNode.desc;
        Object value = frame.getHeap().getStatic(className, fieldName);
        if ("J".equals(fieldDesc)) {
            frame.getOperandStack().pushLong((long) value);
        } else if ("D".equals(fieldDesc)) {
            frame.getOperandStack().pushDouble((double) value);
        } else {
            frame.getOperandStack().push(value);
        }

        //Log.result(frame.getCurrentOpCode(), value, className, fieldName);
    }

}
