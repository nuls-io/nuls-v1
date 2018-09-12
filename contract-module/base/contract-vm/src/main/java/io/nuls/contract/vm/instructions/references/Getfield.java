package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.Descriptors;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.FieldInsnNode;

public class Getfield {

    public static void getfield(Frame frame) {
        FieldInsnNode fieldInsnNode = frame.fieldInsnNode();
        String fieldName = fieldInsnNode.name;
        String fieldDesc = fieldInsnNode.desc;
        ObjectRef objectRef = frame.operandStack.popRef();
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }
        Object value = frame.heap.getField(objectRef, fieldName);
        if (Descriptors.LONG_DESC.equals(fieldDesc)) {
            frame.operandStack.pushLong((long) value);
        } else if (Descriptors.DOUBLE_DESC.equals(fieldDesc)) {
            frame.operandStack.pushDouble((double) value);
        } else {
            frame.operandStack.push(value);
        }

        //Log.result(frame.getCurrentOpCode(), value, objectRef, fieldName);
    }

}
