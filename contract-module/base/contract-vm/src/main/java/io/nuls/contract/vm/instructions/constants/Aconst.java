package io.nuls.contract.vm.instructions.constants;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Aconst {

    public static void aconst_null(final Frame frame) {
        frame.getOperandStack().pushRef(null);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
