package io.nuls.contract.vm.instructions.control;

import io.nuls.contract.vm.Frame;

public class Ret {

    public static void ret(final Frame frame) {
        frame.nonsupportOpCode();
    }

}
