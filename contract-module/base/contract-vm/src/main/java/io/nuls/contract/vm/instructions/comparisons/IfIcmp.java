package io.nuls.contract.vm.instructions.comparisons;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class IfIcmp {

    public static void if_icmpeq(Frame frame) {
        int value2 = frame.operandStack.popInt();
        int value1 = frame.operandStack.popInt();
        boolean result = value1 == value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "==", value2);
    }

    public static void if_icmpne(Frame frame) {
        int value2 = frame.operandStack.popInt();
        int value1 = frame.operandStack.popInt();
        boolean result = value1 != value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "!=", value2);
    }

    public static void if_icmplt(Frame frame) {
        int value2 = frame.operandStack.popInt();
        int value1 = frame.operandStack.popInt();
        boolean result = value1 < value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "<", value2);
    }

    public static void if_icmpge(Frame frame) {
        int value2 = frame.operandStack.popInt();
        int value1 = frame.operandStack.popInt();
        boolean result = value1 >= value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, ">=", value2);
    }

    public static void if_icmpgt(Frame frame) {
        int value2 = frame.operandStack.popInt();
        int value1 = frame.operandStack.popInt();
        boolean result = value1 > value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, ">", value2);
    }

    public static void if_icmple(Frame frame) {
        int value2 = frame.operandStack.popInt();
        int value1 = frame.operandStack.popInt();
        boolean result = value1 <= value2;
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "<=", value2);
    }

}
