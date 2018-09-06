package io.nuls.contract.vm.instructions.math;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Neg {

    public static void ineg(final Frame frame) {
        int value = frame.getOperandStack().popInt();
        int result = -value;
        frame.getOperandStack().pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

    public static void lneg(final Frame frame) {
        long value = frame.getOperandStack().popLong();
        long result = -value;
        frame.getOperandStack().pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

    public static void fneg(final Frame frame) {
        float value = frame.getOperandStack().popFloat();
        float result = -value;
        frame.getOperandStack().pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

    public static void dneg(final Frame frame) {
        double value = frame.getOperandStack().popDouble();
        double result = -value;
        frame.getOperandStack().pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

}
