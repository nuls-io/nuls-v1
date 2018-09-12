package io.nuls.contract.vm.instructions.comparisons;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Dcmp {

    public static void dcmpl(Frame frame) {
        double value2 = frame.operandStack.popDouble();
        double value1 = frame.operandStack.popDouble();
        int result;
        if (Double.isNaN(value1) || Double.isNaN(value2)) {
            result = -1;
        } else {
            result = Double.compare(value1, value2);
        }
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "compare", value2);
    }

    public static void dcmpg(Frame frame) {
        double value2 = frame.operandStack.popDouble();
        double value1 = frame.operandStack.popDouble();
        int result;
        if (Double.isNaN(value1) || Double.isNaN(value2)) {
            result = 1;
        } else {
            result = Double.compare(value1, value2);
        }
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "compare", value2);
    }

}
