package io.nuls.contract.vm.code;

import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;

public class LocalVariableCode {

    /**
     * The name of a local variable.
     */
    public final String name;

    /**
     * The type descriptor of this local variable.
     */
    public final String desc;

    /**
     * The signature of this local variable. May be <tt>null</tt>.
     */
    public final String signature;

    /**
     * The first instruction corresponding to the scope of this local variable (inclusive).
     */
    public final LabelNode start;

    /**
     * The last instruction corresponding to the scope of this local variable (exclusive).
     */
    public final LabelNode end;

    /**
     * The local variable's index.
     */
    public final int index;

    public final VariableType variableType;

    public LocalVariableCode(LocalVariableNode localVariableNode) {
        name = localVariableNode.name;
        desc = localVariableNode.desc;
        signature = localVariableNode.signature;
        start = localVariableNode.start;
        end = localVariableNode.end;
        index = localVariableNode.index;
        //
        variableType = VariableType.valueOf(desc);
    }

}
