package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.FieldInsnNode;

public class Putfield {

    public static void putfield(Frame frame) {
        FieldInsnNode fieldInsnNode = frame.fieldInsnNode();
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
        ObjectRef objectRef = frame.getOperandStack().popRef();
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }
        frame.getHeap().putField(objectRef, fieldName, value);

        //Log.result(frame.getCurrentOpCode(), value, objectRef, fieldName);
    }

}
