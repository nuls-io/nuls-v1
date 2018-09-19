package io.nuls.contract.vm.instructions.math;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Rem {

    public static void irem(final Frame frame) {
        int value2 = frame.operandStack.popInt();
        int value1 = frame.operandStack.popInt();
        int result = value1 % value2;
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "%", value2);
    }

    public static void lrem(final Frame frame) {
        long value2 = frame.operandStack.popLong();
        long value1 = frame.operandStack.popLong();
        long result = value1 % value2;
        frame.operandStack.pushLong(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "%", value2);
    }

    public static void frem(final Frame frame) {
        float value2 = frame.operandStack.popFloat();
        float value1 = frame.operandStack.popFloat();
        float result = value1 % value2;
        frame.operandStack.pushFloat(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "%", value2);
    }

    public static void drem(final Frame frame) {
        double value2 = frame.operandStack.popDouble();
        double value1 = frame.operandStack.popDouble();
        double result = value1 % value2;
        frame.operandStack.pushDouble(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "%", value2);
    }

}
