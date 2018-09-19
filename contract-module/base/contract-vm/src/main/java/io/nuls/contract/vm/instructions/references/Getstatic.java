package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.code.Descriptors;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.FieldInsnNode;

public class Getstatic {

    public static void getstatic(Frame frame) {
        FieldInsnNode fieldInsnNode = frame.fieldInsnNode();
        String className = fieldInsnNode.owner;
        String fieldName = fieldInsnNode.name;
        String fieldDesc = fieldInsnNode.desc;
        Object value = frame.heap.getStatic(className, fieldName);
        if (Descriptors.LONG_DESC.equals(fieldDesc)) {
            frame.operandStack.pushLong((long) value);
        } else if (Descriptors.DOUBLE_DESC.equals(fieldDesc)) {
            frame.operandStack.pushDouble((double) value);
        } else {
            frame.operandStack.push(value);
        }

        //Log.result(frame.getCurrentOpCode(), value, className, fieldName);
    }

}
