package io.nuls.contract.vm.instructions.math;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Neg {

    public static void ineg(final Frame frame) {
        int value = frame.operandStack.popInt();
        int result = -value;
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

    public static void lneg(final Frame frame) {
        long value = frame.operandStack.popLong();
        long result = -value;
        frame.operandStack.pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

    public static void fneg(final Frame frame) {
        float value = frame.operandStack.popFloat();
        float result = -value;
        frame.operandStack.pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

    public static void dneg(final Frame frame) {
        double value = frame.operandStack.popDouble();
        double result = -value;
        frame.operandStack.pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, "-", value);
    }

}
