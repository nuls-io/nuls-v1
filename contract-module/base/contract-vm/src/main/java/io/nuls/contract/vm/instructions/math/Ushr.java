package io.nuls.contract.vm.instructions.math;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Ushr {

    public static void iushr(final Frame frame) {
        int value2 = frame.getOperandStack().popInt();
        int value1 = frame.getOperandStack().popInt();
        int result = value1 >>> value2;
        frame.getOperandStack().pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, ">>>", value2);
    }

    public static void lushr(final Frame frame) {
        int value2 = frame.getOperandStack().popInt();
        long value1 = frame.getOperandStack().popLong();
        long result = value1 >>> value2;
        frame.getOperandStack().pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, ">>>", value2);
    }

}
