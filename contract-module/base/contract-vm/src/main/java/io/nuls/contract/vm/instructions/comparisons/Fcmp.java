package io.nuls.contract.vm.instructions.comparisons;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Fcmp {

    public static void fcmpl(Frame frame) {
        float value2 = frame.operandStack.popFloat();
        float value1 = frame.operandStack.popFloat();
        int result;
        if (Float.isNaN(value1) || Float.isNaN(value2)) {
            result = -1;
        } else {
            result = Float.compare(value1, value2);
        }
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "compare", value2);
    }

    public static void fcmpg(Frame frame) {
        float value2 = frame.operandStack.popFloat();
        float value1 = frame.operandStack.popFloat();
        int result;
        if (Float.isNaN(value1) || Float.isNaN(value2)) {
            result = 1;
        } else {
            result = Float.compare(value1, value2);
        }
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "compare", value2);
    }

}
