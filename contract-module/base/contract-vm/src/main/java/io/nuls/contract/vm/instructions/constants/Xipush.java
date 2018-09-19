package io.nuls.contract.vm.instructions.constants;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Xipush {

    public static void bipush(final Frame frame) {
        xipush(frame);
    }

    public static void sipush(final Frame frame) {
        xipush(frame);
    }

    private static void xipush(final Frame frame) {
        int value = frame.intInsnNode().operand;
        frame.operandStack.pushInt(value);

        //Log.opcode(frame.getCurrentOpCode(), value);
    }

}
