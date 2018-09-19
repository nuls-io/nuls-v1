package io.nuls.contract.vm.instructions.conversions;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class L2x {

    public static void l2i(Frame frame) {
        long value = frame.operandStack.popLong();
        int result = (int) value;
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void l2f(Frame frame) {
        long value = frame.operandStack.popLong();
        float result = (float) value;
        frame.operandStack.pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void l2d(Frame frame) {
        long value = frame.operandStack.popLong();
        double result = (double) value;
        frame.operandStack.pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

}
