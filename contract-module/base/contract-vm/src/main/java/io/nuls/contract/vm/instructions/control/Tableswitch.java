package io.nuls.contract.vm.instructions.control;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

public class Tableswitch {

    public static void tableswitch(final Frame frame) {
        TableSwitchInsnNode table = frame.tableSwitchInsnNode();
        LabelNode labelNode = table.dflt;
        int index = frame.operandStack.popInt();
        int min = table.min;
        int max = table.max;
        int size = max - min + 1;
        for (int i = 0; i < size; i++) {
            if (index == (i + min)) {
                labelNode = table.labels.get(i);
                break;
            }
        }
        frame.jump(labelNode);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
