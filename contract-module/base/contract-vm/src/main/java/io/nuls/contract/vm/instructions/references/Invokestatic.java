package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.MethodInsnNode;

public class Invokestatic {

    public static void invokestatic(Frame frame) {
        MethodInsnNode methodInsnNode = frame.methodInsnNode();
        String className = methodInsnNode.owner;
        String methodName = methodInsnNode.name;
        String methodDesc = methodInsnNode.desc;

        MethodCode methodCode = frame.methodArea.loadMethod(className, methodName, methodDesc);

        MethodArgs methodArgs = new MethodArgs(methodCode.argsVariableType, frame.operandStack, true);

        //Log.opcode(frame.getCurrentOpCode(), className, methodName, methodDesc);

        Result result = NativeMethod.run(methodCode, methodArgs, frame);
        if (result != null) {
            return;
        }

        frame.vm.run(methodCode, methodArgs.frameArgs, true);
    }

}
