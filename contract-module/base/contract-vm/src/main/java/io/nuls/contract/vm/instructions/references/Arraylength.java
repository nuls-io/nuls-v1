package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Arraylength {

    public static void arraylength(Frame frame) {
        ObjectRef arrayRef = frame.operandStack.popRef();
        if (arrayRef == null) {
            frame.throwNullPointerException();
            return;
        }
        int length = arrayRef.getDimensions()[0];
        frame.operandStack.pushInt(length);

        //Log.result(frame.getCurrentOpCode(), length);
    }

}
