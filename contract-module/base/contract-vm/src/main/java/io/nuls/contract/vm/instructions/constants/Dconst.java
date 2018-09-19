package io.nuls.contract.vm.instructions.constants;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Dconst {

    public static void dconst_0(Frame frame) {
        dconst(frame, 0.0D);
    }

    public static void dconst_1(Frame frame) {
        dconst(frame, 1.0D);
    }

    private static void dconst(Frame frame, double value) {
        frame.operandStack.pushDouble(value);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
