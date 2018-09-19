package io.nuls.contract.vm.instructions.comparisons;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;

public class Lcmp {

    public static void lcmp(Frame frame) {
        long value2 = frame.operandStack.popLong();
        long value1 = frame.operandStack.popLong();
        int result = Long.compare(value1, value2);
        frame.operandStack.pushInt(result);

        //Log.result(frame.getCurrentOpCode(), result, value1, "compare", value2);
    }

}
