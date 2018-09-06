package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Instanceof {

    public static void instanceof_(Frame frame) {
        TypeInsnNode typeInsnNode = frame.typeInsnNode();
        VariableType variableType = VariableType.valueOf(typeInsnNode.desc);
        ObjectRef objectRef = frame.getOperandStack().popRef();
        boolean result = instanceof_(objectRef, variableType, frame);
        frame.getOperandStack().pushInt(result ? 1 : 0);

        //Log.result(frame.getCurrentOpCode(), result, objectRef, variableType);
    }

    public static boolean instanceof_(ObjectRef objectRef, VariableType variableType, Frame frame) {
        boolean result;
        if (objectRef == null) {
            result = false;
        } else if (Objects.equals("Ljava/lang/Object;", variableType.getDesc())) {
            result = true;
        } else if (objectRef.isArray() || variableType.isArray()) {
            if (objectRef.isArray() && variableType.isArray()) {
                if (objectRef.getDimensions().length == variableType.getDimensions()) {
                    result = instanceof_(objectRef.getVariableType().getType(), variableType.getType(), frame);
                } else {
                    result = false;
                }
            } else {
                result = false;
            }
        } else {
            result = instanceof_(objectRef.getVariableType().getType(), variableType.getType(), frame);
        }
        return result;
    }

    public static boolean instanceof_(String refType, String className, Frame frame) {
        if (Objects.equals(refType, className) || Objects.equals("java/lang/Object", className)) {
            return true;
        } else if (Objects.equals("java/lang/Object", refType)) {
            return false;
        } else {
            ClassCode classCode = frame.getMethodArea().loadClass(refType);
            String superName = classCode.getSuperName();
            List<String> list = new ArrayList<>();
            list.add(superName);
            list.addAll(classCode.getInterfaces());

            for (String s : list) {
                boolean result = instanceof_(s, className, frame);
                if (result) {
                    return true;
                }
            }
            return false;
        }
    }

}
