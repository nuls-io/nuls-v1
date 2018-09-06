package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.Opcodes;

public class Newarray {

    public static void newarray(Frame frame) {
        VariableType type;
        int length = frame.getOperandStack().popInt();
        if (length < 0) {
            frame.throwNegativeArraySizeException();
            return;
        } else {
            switch (frame.intInsnNode().operand) {
                case Opcodes.T_BOOLEAN:
                    type = VariableType.BOOLEAN_ARRAY_TYPE;
                    break;
                case Opcodes.T_CHAR:
                    type = VariableType.CHAR_ARRAY_TYPE;
                    break;
                case Opcodes.T_FLOAT:
                    type = VariableType.FLOAT_ARRAY_TYPE;
                    break;
                case Opcodes.T_DOUBLE:
                    type = VariableType.DOUBLE_ARRAY_TYPE;
                    break;
                case Opcodes.T_BYTE:
                    type = VariableType.BYTE_ARRAY_TYPE;
                    break;
                case Opcodes.T_SHORT:
                    type = VariableType.SHORT_ARRAY_TYPE;
                    break;
                case Opcodes.T_INT:
                    type = VariableType.INT_ARRAY_TYPE;
                    break;
                case Opcodes.T_LONG:
                    type = VariableType.LONG_ARRAY_TYPE;
                    break;
                default:
                    throw new IllegalArgumentException("unknown operand");
            }
        }

        ObjectRef arrayRef = frame.getHeap().newArray(type, length);
        frame.getOperandStack().pushRef(arrayRef);

        //Log.result(frame.getCurrentOpCode(), arrayRef);
    }

}
