package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;

public class Invokedynamic {

    public static void invokedynamic(Frame frame) {
        frame.nonsupportOpCode();
    }

}
