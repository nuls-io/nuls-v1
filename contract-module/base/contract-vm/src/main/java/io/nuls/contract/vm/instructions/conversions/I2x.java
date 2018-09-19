package io.nuls.contract.vm.instructions.conversions;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class I2x {

    public static void i2l(Frame frame) {
        int value = frame.operandStack.popInt();
        long result = (long) value;
        frame.operandStack.pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2f(Frame frame) {
        int value = frame.operandStack.popInt();
        float result = (float) value;
        frame.operandStack.pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2d(Frame frame) {
        int value = frame.operandStack.popInt();
        double result = (double) value;
        frame.operandStack.pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2b(Frame frame) {
        int value = frame.operandStack.popInt();
        byte result = (byte) value;
        frame.operandStack.pushByte(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2c(Frame frame) {
        int value = frame.operandStack.popInt();
        char result = (char) value;
        frame.operandStack.pushChar(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

    public static void i2s(Frame frame) {
        int value = frame.operandStack.popInt();
        short result = (short) value;
        frame.operandStack.pushShort(result);

        //Log.result(frame.getCurrentOpCode(), result, value);
    }

}
