package io.nuls.contract.vm.instructions.constants;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Fconst {

    public static void fconst_0(final Frame frame) {
        fconst(frame, 0.0F);
    }

    public static void fconst_1(final Frame frame) {
        fconst(frame, 1.0F);
    }

    public static void fconst_2(final Frame frame) {
        fconst(frame, 2.0F);
    }

    private static void fconst(Frame frame, float value) {
        frame.getOperandStack().pushFloat(value);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
