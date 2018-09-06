package io.nuls.contract.vm.instructions.conversions;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class I2x {

    public static void i2l(Frame frame) {
        int value = frame.getOperandStack().popInt();
        long result = (long) value;
        frame.getOperandStack().pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2f(Frame frame) {
        int value = frame.getOperandStack().popInt();
        float result = (float) value;
        frame.getOperandStack().pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2d(Frame frame) {
        int value = frame.getOperandStack().popInt();
        double result = (double) value;
        frame.getOperandStack().pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2b(Frame frame) {
        int value = frame.getOperandStack().popInt();
        byte result = (byte) value;
        frame.getOperandStack().pushByte(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2c(Frame frame) {
        int value = frame.getOperandStack().popInt();
        char result = (char) value;
        frame.getOperandStack().pushChar(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2s(Frame frame) {
        int value = frame.getOperandStack().popInt();
        short result = (short) value;
        frame.getOperandStack().pushShort(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

}
