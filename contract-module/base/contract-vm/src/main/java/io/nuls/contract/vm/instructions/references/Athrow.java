package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.util.Constants;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Athrow {

    public static void athrow(final Frame frame) {
        ObjectRef objectRef = frame.operandStack.popRef();
        if (objectRef == null) {
            frame.throwNullPointerException();
            return;
        }
        //Log.opcode(frame.getCurrentOpCode(), objectRef);
        while (frame.vm.isNotEmptyFrame()) {
            final Frame lastFrame = frame.vm.lastFrame();
            List<TryCatchBlockNode> tryCatchBlockNodes = getTryCatchBlockNodes(lastFrame, objectRef);
            if (tryCatchBlockNodes.size() > 0) {
                TryCatchBlockNode tryCatchBlockNode = tryCatchBlockNodes.get(0);
                lastFrame.operandStack.clear();
                lastFrame.operandStack.pushRef(objectRef);
                lastFrame.jump(tryCatchBlockNode.handler);
                return;
            } else {
                frame.vm.popFrame();
            }
        }
        frame.vm.getResult().exception(objectRef);
    }

    private static List<TryCatchBlockNode> getTryCatchBlockNodes(Frame frame, ObjectRef objectRef) {
        return frame.methodCode.tryCatchBlocks.stream().filter(tryCatchBlockNode -> {
            String type = tryCatchBlockNode.type;
            if (type != null) {
                int line = frame.getLine();
                int start = frame.getLine(tryCatchBlockNode.start);
                int end = frame.getLine(tryCatchBlockNode.end);
                boolean result = start <= line && line < end;
                if (result && extends_(objectRef.getVariableType().getType(), type, frame)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }).collect(Collectors.toList());
    }

    private static boolean extends_(String refType, String className, Frame frame) {
        if (Objects.equals(refType, className)) {
            return true;
        } else {
            ClassCode classCode = frame.methodArea.loadClass(refType);
            String superName = classCode.superName;
            if (Constants.OBJECT_CLASS_NAME.equals(superName)) {
                return false;
            } else {
                return extends_(superName, className, frame);
            }
        }
    }

}
