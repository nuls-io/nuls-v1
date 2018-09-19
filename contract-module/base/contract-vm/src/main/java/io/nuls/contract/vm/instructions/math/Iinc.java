package io.nuls.contract.vm.instructions.math;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.IincInsnNode;

public class Iinc {

    public static void iinc(final Frame frame) {
        IincInsnNode iincInsnNode = frame.iincInsnNode();
        int index = iincInsnNode.var;
        int incr = iincInsnNode.incr;
        int value = frame.localVariables.getInt(index);
        int result = value + incr;
        frame.localVariables.setInt(index, result);

        //Log.result(frame.getCurrentOpCode(), result, value, "+", incr);
    }

}
