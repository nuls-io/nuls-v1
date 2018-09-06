package io.nuls.contract.vm.instructions.constants;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Lconst {

    public static void lconst_0(final Frame frame) {
        lconst(frame, 0L);
    }

    public static void lconst_1(final Frame frame) {
        lconst(frame, 1L);
    }

    private static void lconst(Frame frame, long value) {
        frame.getOperandStack().pushLong(value);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
