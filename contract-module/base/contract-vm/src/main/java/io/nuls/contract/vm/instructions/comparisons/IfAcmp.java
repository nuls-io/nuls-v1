package io.nuls.contract.vm.instructions.comparisons;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

import java.util.Objects;

public class IfAcmp {

    public static void if_acmpeq(Frame frame) {
        ObjectRef value2 = frame.getOperandStack().popRef();
        ObjectRef value1 = frame.getOperandStack().popRef();
        boolean result = Objects.equals(value1, value2);
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "==", value2);
    }

    public static void if_acmpne(Frame frame) {
        ObjectRef value2 = frame.getOperandStack().popRef();
        ObjectRef value1 = frame.getOperandStack().popRef();
        boolean result = !Objects.equals(value1, value2);
        if (result) {
            frame.jump();
        }

        //Log.result(frame.getCurrentOpCode(), result, value1, "!=", value2);
    }

}
