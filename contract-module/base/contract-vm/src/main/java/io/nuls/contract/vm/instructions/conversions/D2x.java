package io.nuls.contract.vm.instructions.conversions;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class D2x {

    public static void d2i(Frame frame) {
        double value = frame.operandStack.popDouble();
        int result = (int) value;
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void d2l(Frame frame) {
        double value = frame.operandStack.popDouble();
        long result = (long) value;
        frame.operandStack.pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void d2f(Frame frame) {
        double value = frame.operandStack.popDouble();
        float result = (float) value;
        frame.operandStack.pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

}
