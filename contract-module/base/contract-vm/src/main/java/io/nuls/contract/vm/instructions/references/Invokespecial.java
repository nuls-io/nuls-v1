package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.MethodInsnNode;

public class Invokespecial {

    public static void invokespecial(Frame frame) {
        MethodInsnNode methodInsnNode = frame.methodInsnNode();
        String className = methodInsnNode.owner;
        String methodName = methodInsnNode.name;
        String methodDesc = methodInsnNode.desc;

        MethodCode methodCode = frame.getMethodArea().loadMethod(className, methodName, methodDesc);

        MethodArgs methodArgs = new MethodArgs(methodCode.getArgsVariableType(), frame.getOperandStack(), false);
        ObjectRef objectRef = methodArgs.getObjectRef();

        //Log.opcode(frame.getCurrentOpCode(), className, methodName, methodDesc);

        if ("java/lang/Object".equals(className) && "<init>".equals(methodName)) {
            return;
        }

        Result result = NativeMethod.run(methodCode, methodArgs, frame);
        if (result != null) {
            return;
        }

        frame.getVm().run(methodCode, methodArgs.getFrameArgs(), true);
    }

}
