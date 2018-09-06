package io.nuls.contract.vm.instructions.constants;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.Type;

public class Ldc {

    public static void ldc(final Frame frame) {
        Object value = frame.ldcInsnNode().cst;

        //Log.opcode(frame.getCurrentOpCode(), value);

        if (value instanceof Integer) {
            frame.getOperandStack().pushInt((int) value);
        } else if (value instanceof Long) {
            frame.getOperandStack().pushLong((long) value);
        } else if (value instanceof Float) {
            frame.getOperandStack().pushFloat((float) value);
        } else if (value instanceof Double) {
            frame.getOperandStack().pushDouble((double) value);
        } else if (value instanceof String) {
            String str = (String) value;
            ObjectRef objectRef = frame.getHeap().newString(str);
            frame.getOperandStack().pushRef(objectRef);
        } else if (value instanceof Type) {
            Type type = (Type) value;
            String desc = type.getDescriptor();
            ObjectRef objectRef = frame.getHeap().getClassRef(desc);
            frame.getOperandStack().pushRef(objectRef);
        } else {
            throw new IllegalArgumentException("unknown ldc cst");
        }
    }

}
