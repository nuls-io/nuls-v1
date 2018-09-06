package io.nuls.contract.vm.instructions.control;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Goto {

    public static void goto_(final Frame frame) {
        frame.jump();

        //Log.opcode(frame.getCurrentOpCode());
    }

}
