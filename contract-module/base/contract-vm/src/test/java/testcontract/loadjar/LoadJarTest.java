/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package testcontract.loadjar;

import io.nuls.contract.vm.OpCode;
import io.nuls.contract.vm.code.ClassCode;
import io.nuls.contract.vm.code.ClassCodeLoader;
import io.nuls.contract.vm.code.MethodCode;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static io.nuls.core.tools.str.StringUtils.EMPTY;

/**
 * @author: PierreLuo
 * @date: 2018/12/21
 */
public class LoadJarTest {

    @Test
    public void test() {
        InputStream in = null;
        try {
            //in = new FileInputStream(LoadJarTest.class.getResource("/contract.jar").getFile());
            in = new FileInputStream(LoadJarTest.class.getResource("/nrc20.jar").getFile());
            byte[] contractCode = IOUtils.toByteArray(in);
            Map<String, ClassCode> codeMap = ClassCodeLoader.loadJarCache(contractCode);
            //for(String key : codeMap.keySet()) {
            //    System.out.println("key: " + key);
            //}
            // io/nuls/vote/contract/event/AddItemEvent
            // io/nuls/vote/contract/func/BaseVote
            // io/nuls/vote/contract/VoteContract
            // io/nuls/contract/token/SimpleToken
            //ClassCode classCode = codeMap.get("io/nuls/vote/contract/event/AddItemEvent");
            //ClassCode classCode = codeMap.get("io/nuls/vote/contract/func/BaseVote");
            //ClassCode classCode = codeMap.get("io/nuls/vote/contract/VoteContract");
            ClassCode classCode = codeMap.get("io/nuls/contract/token/SimpleToken");
            List<MethodCode> methods = classCode.methods;
            for (MethodCode methodCode : methods) {
                System.out.println("Method Name: " + methodCode.fullName);
                InsnList instructions = methodCode.instructions;
                ListIterator<AbstractInsnNode> iterator = instructions.iterator();
                AbstractInsnNode node;
                while (iterator.hasNext()) {
                    node = iterator.next();
                    String desc = getDesc(node);
                    int opcode = node.getOpcode();
                    OpCode opCode = OpCode.valueOf(opcode);
                    if (opCode != null) {
                        System.out.println(OpCode.valueOf(opcode) + " " + desc);
                    }
                }
                System.out.println();
                System.out.println();
                System.out.println("////////////////////////////////////////////////////////////////////////");
                System.out.println("////////////////////////////////////////////////////////////////////////");
                System.out.println("////////////////////////////////////////////////////////////////////////");
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String getDesc(AbstractInsnNode node) {
        if (node == null) {
            return EMPTY;
        }
        String result;
        switch (node.getType()) {
            case AbstractInsnNode.FIELD_INSN:
                FieldInsnNode field = (FieldInsnNode) node;
                result = field.name + ":" + field.desc;
                break;
            case AbstractInsnNode.LDC_INSN:
                LdcInsnNode ldc = (LdcInsnNode) node;
                Object value = ldc.cst;
                if (value instanceof Type) {
                    Type type = (Type) value;
                    result = type.getDescriptor();
                } else {
                    result = ldc.cst.toString();
                }
                break;
            case AbstractInsnNode.METHOD_INSN:
                MethodInsnNode method = (MethodInsnNode) node;
                result = method.itf + " " + method.owner + "." + method.name + ":" + method.desc;
                break;
            case AbstractInsnNode.TYPE_INSN:
                TypeInsnNode type = (TypeInsnNode) node;
                result = type.desc;
                break;
            case AbstractInsnNode.VAR_INSN:
                VarInsnNode var = (VarInsnNode) node;
                result = String.valueOf(var.var);
                break;
            case AbstractInsnNode.JUMP_INSN:
                JumpInsnNode jump = (JumpInsnNode) node;
                LabelNode label = jump.label;
                AbstractInsnNode next = label.getNext();
                while (true) {
                    if (next == null) {
                        result = EMPTY;
                        break;
                    }
                    int opcode = next.getOpcode();
                    if(opcode != -1) {
                        OpCode opCode = OpCode.valueOf(opcode);
                        result = opCode.toString();
                        result += " " + getDesc(next);
                        break;
                    }
                    next = next.getNext();
                }
                break;
            case AbstractInsnNode.INT_INSN:
                IntInsnNode intInsnNode = (IntInsnNode) node;
                int operand = intInsnNode.operand;
                result = String.valueOf(operand);
                break;
            case AbstractInsnNode.IINC_INSN:
                IincInsnNode iinc = (IincInsnNode) node;
                result = iinc.var + "," + iinc.incr;
                break;
            //case AbstractInsnNode.LINE:
            //    LineNumberNode lineNUmber = (LineNumberNode) node;
            //    result = "================================: " + lineNUmber.line;
            //    break;
            default:
            result = EMPTY;
        }
        return result;
    }
}
