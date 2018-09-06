package io.nuls.contract.vm.instructions.comparisons;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class IfCmp {

    public static void ifeq(Frame frame) {
        int value1 = frame.getOperandStack().popInt();
        int value2 = 0;
        boolean result = value1 == value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "==", value2);
    }

    public static void ifne(Frame frame) {
        int value1 = frame.getOperandStack().popInt();
        int value2 = 0;
        boolean result = value1 != value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "!=", value2);
    }

    public static void iflt(Frame frame) {
        int value1 = frame.getOperandStack().popInt();
        int value2 = 0;
        boolean result = value1 < value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "<", value2);
    }

    public static void ifge(Frame frame) {
        int value1 = frame.getOperandStack().popInt();
        int value2 = 0;
        boolean result = value1 >= value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, ">=", value2);
    }

    public static void ifgt(Frame frame) {
        int value1 = frame.getOperandStack().popInt();
        int value2 = 0;
        boolean result = value1 > value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, ">", value2);
    }

    public static void ifle(Frame frame) {
        int value1 = frame.getOperandStack().popInt();
        int value2 = 0;
        boolean result = value1 <= value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "<=", value2);
    }

}
