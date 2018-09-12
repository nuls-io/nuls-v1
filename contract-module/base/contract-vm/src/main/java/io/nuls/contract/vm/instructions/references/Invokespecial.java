package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.util.Constants;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.MethodInsnNode;

public class Invokespecial {

    public static void invokespecial(Frame frame) {
        MethodInsnNode methodInsnNode = frame.methodInsnNode();
        String className = methodInsnNode.owner;
        String methodName = methodInsnNode.name;
        String methodDesc = methodInsnNode.desc;

        MethodCode methodCode = frame.methodArea.loadMethod(className, methodName, methodDesc);

        MethodArgs methodArgs = new MethodArgs(methodCode.argsVariableType, frame.operandStack, false);
        ObjectRef objectRef = methodArgs.objectRef;
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }

        //Log.opcode(frame.getCurrentOpCode(), className, methodName, methodDesc);

        if (Constants.OBJECT_CLASS_NAME.equals(className) && Constants.CONSTRUCTOR_NAME.equals(methodName)) {
            return;
        }

        Result result = NativeMethod.run(methodCode, methodArgs, frame);
        if (result != null) {
            return;
        }

        frame.vm.run(methodCode, methodArgs.frameArgs, true);
    }

}
