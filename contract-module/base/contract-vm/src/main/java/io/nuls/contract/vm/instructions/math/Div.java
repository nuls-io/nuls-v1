package io.nuls.contract.vm.instructions.math;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Div {

    public static void idiv(final Frame frame) {
        int value2 = frame.getOperandStack().popInt();
        int value1 = frame.getOperandStack().popInt();
        int result = value1 / value2;
        frame.getOperandStack().pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "/", value2);
    }

    public static void ldiv(final Frame frame) {
        long value2 = frame.getOperandStack().popLong();
        long value1 = frame.getOperandStack().popLong();
        long result = value1 / value2;
        frame.getOperandStack().pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "/", value2);
    }

    public static void fdiv(final Frame frame) {
        float value2 = frame.getOperandStack().popFloat();
        float value1 = frame.getOperandStack().popFloat();
        float result = value1 / value2;
        frame.getOperandStack().pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "/", value2);
    }

    public static void ddiv(final Frame frame) {
        double value2 = frame.getOperandStack().popDouble();
        double value1 = frame.getOperandStack().popDouble();
        double result = value1 / value2;
        frame.getOperandStack().pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "/", value2);
    }

}
