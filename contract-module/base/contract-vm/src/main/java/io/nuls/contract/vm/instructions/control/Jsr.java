package io.nuls.contract.vm.instructions.control;

import io.nuls.contract.vm.Frame;

public class Jsr {

    public static void jsr(final Frame frame) {
        frame.nonsupportOpCode();
    }

}
