package io.nuls.contract.vm.instructions.control;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;

public class Lookupswitch {

    public static void lookupswitch(final Frame frame) {
        LookupSwitchInsnNode lookup = frame.lookupSwitchInsnNode();
        LabelNode labelNode = lookup.dflt;
        int key = frame.getOperandStack().popInt();
        for (int i = 0; i < lookup.keys.size(); i++) {
            int k = lookup.keys.get(i);
            if (k == key) {
                labelNode = lookup.labels.get(i);
                break;
            }
        }
        frame.jump(labelNode);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
