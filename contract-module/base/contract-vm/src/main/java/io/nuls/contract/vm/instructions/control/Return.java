package io.nuls.contract.vm.instructions.control;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.util.Log;

public class Return {

    public static void ireturn(final Frame frame) {
        Object result;
        switch (frame.getResult().getVariableType().getType()) {
            case "boolean":
                result = frame.getOperandStack().popBoolean();
                break;
            case "byte":
                result = frame.getOperandStack().popByte();
                break;
            case "char":
                result = frame.getOperandStack().popChar();
                break;
            case "short":
                result = frame.getOperandStack().popShort();
                break;
            default:
                result = frame.getOperandStack().popInt();
                break;
        }
        frame.getResult().value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void lreturn(final Frame frame) {
        long result = frame.getOperandStack().popLong();
        frame.getResult().value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void freturn(final Frame frame) {
        float result = frame.getOperandStack().popFloat();
        frame.getResult().value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void dreturn(final Frame frame) {
        double result = frame.getOperandStack().popDouble();
        frame.getResult().value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void areturn(final Frame frame) {
        ObjectRef result = frame.getOperandStack().popRef();
        frame.getResult().value(result);

        //Log.result(frame.getCurrentOpCode(), result);
    }

    public static void return_(final Frame frame) {
        frame.getResult().value(null);

        //Log.opcode(frame.getCurrentOpCode());
    }

}
