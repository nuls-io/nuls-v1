package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.FieldInsnNode;

public class Getfield {

    public static void getfield(Frame frame) {
        FieldInsnNode fieldInsnNode = frame.fieldInsnNode();
        String fieldName = fieldInsnNode.name;
        String fieldDesc = fieldInsnNode.desc;
        ObjectRef objectRef = frame.getOperandStack().popRef();
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }
        Object value = frame.getHeap().getField(objectRef, fieldName);
        if ("J".equals(fieldDesc)) {
            frame.getOperandStack().pushLong((long) value);
        } else if ("D".equals(fieldDesc)) {
            frame.getOperandStack().pushDouble((double) value);
        } else {
            frame.getOperandStack().push(value);
        }

        //Log.result(frame.getCurrentOpCode(), value, objectRef, fieldName);
    }

}
