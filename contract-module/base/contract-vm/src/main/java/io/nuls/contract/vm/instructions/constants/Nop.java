package io.nuls.contract.vm.instructions.constants;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Nop {

    public static void nop(Frame frame) {
        //Log.opcode(frame.getCurrentOpCode());
    }

}
