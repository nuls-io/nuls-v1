package io.nuls.contract.vm.instructions.conversions;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class L2x {

    public static void l2i(Frame frame) {
        long value = frame.getOperandStack().popLong();
        int result = (int) value;
        frame.getOperandStack().pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void l2f(Frame frame) {
        long value = frame.getOperandStack().popLong();
        float result = (float) value;
        frame.getOperandStack().pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void l2d(Frame frame) {
        long value = frame.getOperandStack().popLong();
        double result = (double) value;
        frame.getOperandStack().pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

}
