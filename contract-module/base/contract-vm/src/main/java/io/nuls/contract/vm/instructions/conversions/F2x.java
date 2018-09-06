package io.nuls.contract.vm.instructions.conversions;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class F2x {

    public static void f2i(Frame frame) {
        float value = frame.getOperandStack().popFloat();
        int result = (int) value;
        frame.getOperandStack().pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void f2l(Frame frame) {
        float value = frame.getOperandStack().popFloat();
        long result = (long) value;
        frame.getOperandStack().pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void f2d(Frame frame) {
        float value = frame.getOperandStack().popFloat();
        double result = (double) value;
        frame.getOperandStack().pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

}
