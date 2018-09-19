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
            frame.operandStack.pushInt((int) value);
        } else if (value instanceof Long) {
            frame.operandStack.pushLong((long) value);
        } else if (value instanceof Float) {
            frame.operandStack.pushFloat((float) value);
        } else if (value instanceof Double) {
            frame.operandStack.pushDouble((double) value);
        } else if (value instanceof String) {
            String str = (String) value;
            ObjectRef objectRef = frame.heap.newString(str);
            frame.operandStack.pushRef(objectRef);
        } else if (value instanceof Type) {
            Type type = (Type) value;
            String desc = type.getDescriptor();
            ObjectRef objectRef = frame.heap.getClassRef(desc);
            frame.operandStack.pushRef(objectRef);
        } else {
            throw new IllegalArgumentException("unknown ldc cst");
        }
    }

}
