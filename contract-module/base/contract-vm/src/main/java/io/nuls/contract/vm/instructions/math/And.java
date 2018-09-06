package io.nuls.contract.vm.instructions.math;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class And {

    public static void iand(final Frame frame) {
        int value2 = frame.getOperandStack().popInt();
        int value1 = frame.getOperandStack().popInt();
        int result = value1 & value2;
        frame.getOperandStack().pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "&", value2);
    }

    public static void land(final Frame frame) {
        long value2 = frame.getOperandStack().popLong();
        long value1 = frame.getOperandStack().popLong();
        long result = value1 & value2;
        frame.getOperandStack().pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "&", value2);
    }

}
