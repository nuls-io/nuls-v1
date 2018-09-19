package io.nuls.contract.vm.instructions.control;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.Descriptors;
import io.nuls.contract.vm.util.Log;

public class Return {

    public static void ireturn(final Frame frame) {
        Object result;
        switch (frame.result.getVariableType().getType()) {
            case Descriptors.BOOLEAN:
                result = frame.operandStack.popBoolean();
                break;
            case Descriptors.BYTE:
                result = frame.operandStack.popByte();
                break;
            case Descriptors.CHAR:
                result = frame.operandStack.popChar();
                break;
            case Descriptors.SHORT:
                result = frame.operandStack.popShort();
                break;
            default:
                result = frame.operandStack.popInt();
                break;
        }
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void lreturn(final Frame frame) {
        long result = frame.operandStack.popLong();
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void freturn(final Frame frame) {
        float result = frame.operandStack.popFloat();
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void dreturn(final Frame frame) {
        double result = frame.operandStack.popDouble();
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void areturn(final Frame frame) {
        ObjectRef result = frame.operandStack.popRef();
        frame.result.value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void return_(final Frame frame) {
        frame.result.value(null);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
