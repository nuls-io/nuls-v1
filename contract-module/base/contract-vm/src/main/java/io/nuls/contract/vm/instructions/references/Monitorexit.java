package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;

public class Monitorexit {

    public static void monitorexit(Frame frame) {
        frame.nonsupportOpCode();
    }

}
