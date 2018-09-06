package io.nuls.contract.vm.instructions.constants;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Iconst {

    public static void iconst_m1(final Frame frame) {
        iconst(frame, -1);
    }

    public static void iconst_0(final Frame frame) {
        iconst(frame, 0);
    }

    public static void iconst_1(final Frame frame) {
        iconst(frame, 1);
    }

    public static void iconst_2(final Frame frame) {
        iconst(frame, 2);
    }

    public static void iconst_3(final Frame frame) {
        iconst(frame, 3);
    }

    public static void iconst_4(final Frame frame) {
        iconst(frame, 4);
    }

    public static void iconst_5(final Frame frame) {
        iconst(frame, 5);
    }

    private static void iconst(Frame frame, int value) {
        frame.getOperandStack().pushInt(value);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
