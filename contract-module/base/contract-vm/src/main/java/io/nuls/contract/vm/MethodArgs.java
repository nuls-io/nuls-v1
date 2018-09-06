package io.nuls.contract.vm;

import io.nuls.contract.vm.code.VariableType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodArgs {

    private Object[] frameArgs;

    private Object[] invokeArgs;

    private ObjectRef objectRef;

    public MethodArgs(List<VariableType> argsVariableType, OperandStack operandStack, boolean isStatic) {
        int size = argsVariableType.size();
        List frameList = new ArrayList();
        List invokeList = new ArrayList();
        for (int i = size - 1; i >= 0; i--) {
            VariableType variableType = argsVariableType.get(i);
            if (variableType.isLong() || variableType.isDouble()) {
                frameList.add(operandStack.pop());
            }
            Object value = operandStack.pop();
            frameList.add(value);
            invokeList.add(variableType.getPrimitiveValue(value));
        }
        if (!isStatic) {
            this.objectRef = (ObjectRef) operandStack.pop();
            frameList.add(this.objectRef);
        }
        Collections.reverse(frameList);
        Collections.reverse(invokeList);
        this.frameArgs = frameList.toArray();
        this.invokeArgs = invokeList.toArray();
    }

    public Object[] getFrameArgs() {
        return frameArgs;
    }

    public Object[] getInvokeArgs() {
        return invokeArgs;
    }

    public ObjectRef getObjectRef() {
        return objectRef;
    }

}
