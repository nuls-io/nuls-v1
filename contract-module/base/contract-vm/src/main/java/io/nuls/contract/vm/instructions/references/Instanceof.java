/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.vm.instructions.references;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.util.Constants;
import io.nuls.contract.vm.util.Log;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Instanceof {

    public static void instanceof_(Frame frame) {
        TypeInsnNode typeInsnNode = frame.typeInsnNode();
        VariableType variableType = VariableType.valueOf(typeInsnNode.desc);
        ObjectRef objectRef = frame.operandStack.popRef();
        boolean result = instanceof_(objectRef, variableType, frame);
        frame.operandStack.pushInt(result ? 1 : 0);

        //Log.result(frame.getCurrentOpCode(), result, objectRef, variableType);
    }

    public static boolean instanceof_(ObjectRef objectRef, VariableType variableType, Frame frame) {
        boolean result;
        if (objectRef == null) {
            result = false;
        } else if (Objects.equals(Constants.OBJECT_CLASS_DESC, variableType.getDesc())) {
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
        if (Objects.equals(refType, className) || Objects.equals(Constants.OBJECT_CLASS_NAME, className)) {
            return true;
        } else if (Objects.equals(Constants.OBJECT_CLASS_NAME, refType)) {
            return false;
        } else {
            ClassCode classCode = frame.methodArea.loadClass(refType);
            String superName = classCode.superName;
            List<String> list = new ArrayList<>();
            list.add(superName);
            list.addAll(classCode.interfaces);

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
