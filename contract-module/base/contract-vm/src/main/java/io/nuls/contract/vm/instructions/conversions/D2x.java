package io.nuls.contract.vm.instructions.conversions;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class D2x {

    public static void d2i(Frame frame) {
        double value = frame.getOperandStack().popDouble();
        int result = (int) value;
        frame.getOperandStack().pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void d2l(Frame frame) {
        double value = frame.getOperandStack().popDouble();
        long result = (long) value;
        frame.getOperandStack().pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void d2f(Frame frame) {
        double value = frame.getOperandStack().popDouble();
        float result = (float) value;
        frame.getOperandStack().pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

}
