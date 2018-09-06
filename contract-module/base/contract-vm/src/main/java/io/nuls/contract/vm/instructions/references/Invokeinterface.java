package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.List;

public class Invokeinterface {

    public static void invokeinterface(Frame frame) {
        MethodInsnNode methodInsnNode = frame.methodInsnNode();
        String interfaceName = methodInsnNode.owner;
        String interfaceMethodName = methodInsnNode.name;
        String interfaceMethodDesc = methodInsnNode.desc;

        List<VariableType> variableTypes = VariableType.parseArgs(interfaceMethodDesc);
        MethodArgs methodArgs = new MethodArgs(variableTypes, frame.getOperandStack(), false);
        ObjectRef objectRef = methodArgs.getObjectRef();
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }

        String className = objectRef.getVariableType().getType();
        MethodCode methodCode = frame.getMethodArea().loadMethod(className, interfaceMethodName, interfaceMethodDesc);

        //Log.opcode(frame.getCurrentOpCode(), className, interfaceMethodName, interfaceMethodDesc);

        frame.getVm().run(methodCode, methodArgs.getFrameArgs(), true);
    }

}
