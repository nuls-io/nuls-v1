/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.contract.vm;

import io.nuls.contract.entity.BlockHeaderDto;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.exception.ErrorException;
import io.nuls.contract.vm.instructions.comparisons.*;
import io.nuls.contract.vm.instructions.constants.Ldc;
import io.nuls.contract.vm.instructions.control.*;
import io.nuls.contract.vm.instructions.conversions.D2x;
import io.nuls.contract.vm.instructions.conversions.F2x;
import io.nuls.contract.vm.instructions.conversions.I2x;
import io.nuls.contract.vm.instructions.conversions.L2x;
import io.nuls.contract.vm.instructions.extended.Ifnonnull;
import io.nuls.contract.vm.instructions.extended.Ifnull;
import io.nuls.contract.vm.instructions.extended.Multianewarray;
import io.nuls.contract.vm.instructions.loads.*;
import io.nuls.contract.vm.instructions.math.*;
import io.nuls.contract.vm.instructions.references.*;
import io.nuls.contract.vm.instructions.stack.Dup;
import io.nuls.contract.vm.instructions.stack.Pop;
import io.nuls.contract.vm.instructions.stack.Swap;
import io.nuls.contract.vm.instructions.stores.*;
import io.nuls.contract.vm.natives.io.nuls.contract.sdk.NativeAddress;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.ProgramMethodArg;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.contract.vm.program.impl.ProgramContext;
import io.nuls.contract.vm.program.impl.ProgramInvoke;
import io.nuls.contract.vm.util.Log;
import org.apache.commons.lang3.StringUtils;
import org.ethereum.core.Repository;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class VM {

    private Logger log = LoggerFactory.getLogger(VM.class);

    private static final int VM_STACK_MAX_SIZE = 512;

    private static final BigInteger TEN_THOUSAND = new BigInteger("10000");

    public static final int MAX_GAS = 1000_0000;

    public final VMStack vmStack;

    public final Heap heap;

    public final MethodArea methodArea;

    private Result result;

    private Object resultValue;

    private VMContext vmContext;

    private ProgramInvoke programInvoke;

    private ProgramContext programContext;

    private ProgramExecutor programExecutor;

    private Repository repository;

    private long gasUsed;

    private long gas;

    private long startTime;

    private long endTime;

    private long elapsedTime;

    private List<ProgramTransfer> transfers = new ArrayList<>();

    private List<String> events = new ArrayList<>();

    public VM() {
        this.vmStack = new VMStack(VM_STACK_MAX_SIZE);
        this.heap = new Heap(BigInteger.ZERO);
        this.heap.setVm(this);
        this.methodArea = new MethodArea();
        this.methodArea.setVm(this);
        this.result = new Result();
    }

    public VM(VM vm) {
        this.vmStack = new VMStack(VM_STACK_MAX_SIZE);
        if (vm.heap.getObjectRefCount().compareTo(TEN_THOUSAND) > 0) {
            throw new RuntimeException();
        }
        this.heap = new Heap(TEN_THOUSAND);
        this.heap.setVm(this);
        this.methodArea = new MethodArea();
        this.methodArea.setVm(this);
        this.result = new Result();
    }

    public VM(Heap heap, MethodArea methodArea) {
        this.vmStack = new VMStack(VM_STACK_MAX_SIZE);
        this.heap = heap;
        this.heap.setVm(this);
        this.methodArea = methodArea;
        this.methodArea.setVm(this);
        this.result = new Result();
    }

    public boolean isEmptyFrame() {
        return this.vmStack.isEmpty();
    }

    public boolean isNotEmptyFrame() {
        return !isEmptyFrame();
    }

    public Frame lastFrame() {
        return this.vmStack.lastElement();
    }

    public void popFrame() {
        this.vmStack.pop();
    }

    public void endTime() {
        this.endTime = System.currentTimeMillis();
        this.elapsedTime = this.endTime - this.startTime;
    }

    public void initProgramContext(ProgramInvoke programInvoke) {
        this.programInvoke = programInvoke;

        programContext = new ProgramContext();
        programContext.setAddress(this.heap.newAddress(programInvoke.getAddress()));
        if (programInvoke.getSender() != null) {
            programContext.setSender(this.heap.newAddress(NativeAddress.toString(programInvoke.getSender())));
        }
        programContext.setGasPrice(programInvoke.getPrice());
        programContext.setGas(programInvoke.getGasLimit());
        programContext.setValue(this.heap.newBigInteger(programInvoke.getValue().toString()));
        programContext.setNumber(programInvoke.getNumber());
        programContext.setEstimateGas(programInvoke.isEstimateGas());
    }

    private static final String CLASS_NAME = "java/util/HashMap";
    private static final String METHOD_NAME = "resize";
    private static final String METHOD_DESC = "()[Ljava/util/HashMap$Node;";

    public void run(MethodCode methodCode, Object[] args, boolean pushResult) {
        Frame frame = new Frame(this, methodCode, args);
        if (methodCode.isMethod(CLASS_NAME, METHOD_NAME, METHOD_DESC)) {
            frame.setAddGas(false);
        }
        this.vmStack.push(frame);
        run(pushResult);
        if (!frame.addGas) {
            frame.setAddGas(true);
        }
    }

    public void run(ObjectRef objectRef, MethodCode methodCode, VMContext vmContext, ProgramInvoke programInvoke) {
        this.vmContext = vmContext;
        Object[] runArgs = runArgs(objectRef, methodCode, programInvoke.getArgs());
        if (isEnd()) {
            return;
        }
        initProgramContext(programInvoke);
        run(methodCode, runArgs, true);
    }

    private Object[] runArgs(ObjectRef objectRef, MethodCode methodCode, String[][] args) {
        final List runArgs = new ArrayList();
        runArgs.add(objectRef);
        final List<VariableType> argsVariableType = methodCode.argsVariableType;
        for (int i = 0; i < argsVariableType.size(); i++) {
            final VariableType variableType = argsVariableType.get(i);
            final ProgramMethodArg programMethodArg = methodCode.args.get(i);
            final String[] arg = args[i];
            String realArg = null;
            if (arg != null && arg.length > 0) {
                realArg = arg[0];
            }
            if (programMethodArg.isRequired()) {
                if (arg == null || arg.length < 1 || (!variableType.isArray() && StringUtils.isEmpty(realArg))) {
                    throw new RuntimeException(String.format("parameter %s required", programMethodArg.getName()));
                }
            }
            if (arg == null || arg.length == 0) {
                runArgs.add(null);
            } else if (variableType.isArray()) {
                if (arg.length < 1) {
                    runArgs.add(null);
                } else if (variableType.isPrimitiveType()) {
                    Object array = Array.newInstance(variableType.getComponentType().getPrimitiveTypeClass(), arg.length);
                    for (int j = 0; j < arg.length; j++) {
                        String item = arg[j];
                        Object value = variableType.getComponentType().getPrimitiveValue(item);
                        Array.set(array, j, value);
                    }
                    final ObjectRef ref = this.heap.newArray(array, variableType, arg.length);
                    runArgs.add(ref);
                } else if (variableType.getComponentType().isWrapperType()) {
                    ObjectRef arrayRef = this.heap.newArray(variableType, arg.length);
                    for (int j = 0; j < arg.length; j++) {
                        String item = arg[j];
                        if (item == null) {
                            continue;
                        }
                        ObjectRef ref;
                        if (VariableType.CHAR_WRAPPER_TYPE.equals(variableType.getComponentType())) {
                            ref = this.heap.newCharacter(item.charAt(0));
                        } else {
                            ref = this.heap.runNewObject(variableType.getComponentType(), item);
                        }
                        if (isEnd()) {
                            return null;
                        }
                        this.heap.putArray(arrayRef, j, ref);
                    }
                    runArgs.add(arrayRef);
                } else {
                    ObjectRef arrayRef = this.heap.newArray(VariableType.STRING_ARRAY_TYPE, arg.length);
                    for (int j = 0; j < arg.length; j++) {
                        String item = arg[j];
                        ObjectRef ref = this.heap.newString(item);
                        this.heap.putArray(arrayRef, j, ref);
                    }
                    runArgs.add(arrayRef);
                }
            } else if (variableType.isPrimitive()) {
                final Object primitiveValue = variableType.getPrimitiveValue(realArg);
                runArgs.add(primitiveValue);
                if (variableType.isLong() || variableType.isDouble()) {
                    runArgs.add(null);
                }
            } else if (VariableType.STRING_TYPE.equals(variableType)) {
                final ObjectRef ref = this.heap.newString(realArg);
                runArgs.add(ref);
            } else {
                final ObjectRef ref = this.heap.runNewObject(variableType, realArg);
                if (isEnd()) {
                    return null;
                }
                runArgs.add(ref);
            }
        }
        return runArgs.toArray();
    }

    public void run(boolean pushResult) {
        if (this.startTime < 1) {
            this.startTime = System.currentTimeMillis();
        }
        if (this.result.isError()) {
            endTime();
            return;
        }
        if (!this.vmStack.isEmpty()) {
            final Frame frame = this.vmStack.lastElement();
            //Log.runMethod(frame.methodCode);
            while (frame.getCurrentInsnNode() != null && !frame.result.isEnded()) {
                step(frame);
                frame.step();
                if (isEnd()) {
                    return;
                }
                if (frame != this.vmStack.lastElement()) {
                    endTime();
                    return;
                }
            }
            this.popFrame();
            //Log.endMethod(frame.methodCode);
            this.resultValue = frame.result.getValue();
            if (!this.vmStack.isEmpty()) {
                final Frame lastFrame = this.vmStack.lastElement();
                if (frame.result.getVariableType().isNotVoid()) {
                    if (pushResult) {
                        lastFrame.operandStack.push(frame.result.getValue(), frame.result.getVariableType());
                    }
                }
                //Log.continueMethod(lastFrame.methodCode);
            } else {
                this.result = frame.result;
            }
        }
        endTime();
    }

    private boolean isEnd() {
        if (this.result.isError()) {
            endTime();
            return true;
        }
        if (this.result.isException()) {
            endTime();
            return true;
        }
        return false;
    }

    private void step(Frame frame) {

        OpCode opCode = frame.currentOpCode();

        if (opCode == null) {
            if (frame.getCurrentInsnNode() != null && frame.getCurrentInsnNode().getOpcode() >= 0) {
                frame.nonsupportOpCode();
            }
            return;
        }

        if (frame.addGas) {
            int gasCost = gasCost(frame, opCode);
            addGasUsed(gasCost);
        }

        switch (opCode) {
            case NOP:
                //Nop.nop(frame);
                break;
            case ACONST_NULL:
                //Aconst.aconst_null(frame);
                frame.operandStack.pushRef(null);
                break;
            case ICONST_M1:
                //Iconst.iconst_m1(frame);
                frame.operandStack.pushInt(-1);
                break;
            case ICONST_0:
                //Iconst.iconst_0(frame);
                frame.operandStack.pushInt(0);
                break;
            case ICONST_1:
                //Iconst.iconst_1(frame);
                frame.operandStack.pushInt(1);
                break;
            case ICONST_2:
                //Iconst.iconst_2(frame);
                frame.operandStack.pushInt(2);
                break;
            case ICONST_3:
                //Iconst.iconst_3(frame);
                frame.operandStack.pushInt(3);
                break;
            case ICONST_4:
                //Iconst.iconst_4(frame);
                frame.operandStack.pushInt(4);
                break;
            case ICONST_5:
                //Iconst.iconst_5(frame);
                frame.operandStack.pushInt(5);
                break;
            case LCONST_0:
                //Lconst.lconst_0(frame);
                frame.operandStack.pushLong(0L);
                break;
            case LCONST_1:
                //Lconst.lconst_1(frame);
                frame.operandStack.pushLong(1L);
                break;
            case FCONST_0:
                //Fconst.fconst_0(frame);
                frame.operandStack.pushFloat(0.0F);
                break;
            case FCONST_1:
                //Fconst.fconst_1(frame);
                frame.operandStack.pushFloat(1.0F);
                break;
            case FCONST_2:
                //Fconst.fconst_2(frame);
                frame.operandStack.pushFloat(2.0F);
                break;
            case DCONST_0:
                //Dconst.dconst_0(frame);
                frame.operandStack.pushDouble(0.0D);
                break;
            case DCONST_1:
                //Dconst.dconst_1(frame);
                frame.operandStack.pushDouble(1.0D);
                break;
            case BIPUSH:
                //Xipush.bipush(frame);
                frame.operandStack.pushInt(frame.intInsnNode().operand);
                break;
            case SIPUSH:
                //Xipush.sipush(frame);
                frame.operandStack.pushInt(frame.intInsnNode().operand);
                break;
            case LDC:
                Ldc.ldc(frame);
                break;
            case ILOAD:
                Iload.iload(frame);
                break;
            case LLOAD:
                Lload.lload(frame);
                break;
            case FLOAD:
                Fload.fload(frame);
                break;
            case DLOAD:
                Dload.dload(frame);
                break;
            case ALOAD:
                Aload.aload(frame);
                break;
            case IALOAD:
                Xaload.iaload(frame);
                break;
            case LALOAD:
                Xaload.laload(frame);
                break;
            case FALOAD:
                Xaload.faload(frame);
                break;
            case DALOAD:
                Xaload.daload(frame);
                break;
            case AALOAD:
                Xaload.aaload(frame);
                break;
            case BALOAD:
                Xaload.baload(frame);
                break;
            case CALOAD:
                Xaload.caload(frame);
                break;
            case SALOAD:
                Xaload.saload(frame);
                break;
            case ISTORE:
                Istore.istore(frame);
                break;
            case LSTORE:
                Lstore.lstore(frame);
                break;
            case FSTORE:
                Fstore.fstore(frame);
                break;
            case DSTORE:
                Dstore.dstore(frame);
                break;
            case ASTORE:
                Astore.astore(frame);
                break;
            case IASTORE:
                Xastore.iastore(frame);
                break;
            case LASTORE:
                Xastore.lastore(frame);
                break;
            case FASTORE:
                Xastore.fastore(frame);
                break;
            case DASTORE:
                Xastore.dastore(frame);
                break;
            case AASTORE:
                Xastore.aastore(frame);
                break;
            case BASTORE:
                Xastore.bastore(frame);
                break;
            case CASTORE:
                Xastore.castore(frame);
                break;
            case SASTORE:
                Xastore.sastore(frame);
                break;
            case POP:
                Pop.pop(frame);
                break;
            case POP2:
                Pop.pop2(frame);
                break;
            case DUP:
                Dup.dup(frame);
                break;
            case DUP_X1:
                Dup.dup_x1(frame);
                break;
            case DUP_X2:
                Dup.dup_x2(frame);
                break;
            case DUP2:
                Dup.dup2(frame);
                break;
            case DUP2_X1:
                Dup.dup2_x1(frame);
                break;
            case DUP2_X2:
                Dup.dup2_x2(frame);
                break;
            case SWAP:
                Swap.swap(frame);
                break;
            case IADD:
                Add.iadd(frame);
                break;
            case LADD:
                Add.ladd(frame);
                break;
            case FADD:
                Add.fadd(frame);
                break;
            case DADD:
                Add.dadd(frame);
                break;
            case ISUB:
                Sub.isub(frame);
                break;
            case LSUB:
                Sub.lsub(frame);
                break;
            case FSUB:
                Sub.fsub(frame);
                break;
            case DSUB:
                Sub.dsub(frame);
                break;
            case IMUL:
                Mul.imul(frame);
                break;
            case LMUL:
                Mul.lmul(frame);
                break;
            case FMUL:
                Mul.fmul(frame);
                break;
            case DMUL:
                Mul.dmul(frame);
                break;
            case IDIV:
                Div.idiv(frame);
                break;
            case LDIV:
                Div.ldiv(frame);
                break;
            case FDIV:
                Div.fdiv(frame);
                break;
            case DDIV:
                Div.ddiv(frame);
                break;
            case IREM:
                Rem.irem(frame);
                break;
            case LREM:
                Rem.lrem(frame);
                break;
            case FREM:
                Rem.frem(frame);
                break;
            case DREM:
                Rem.drem(frame);
                break;
            case INEG:
                Neg.ineg(frame);
                break;
            case LNEG:
                Neg.lneg(frame);
                break;
            case FNEG:
                Neg.fneg(frame);
                break;
            case DNEG:
                Neg.dneg(frame);
                break;
            case ISHL:
                Shl.ishl(frame);
                break;
            case LSHL:
                Shl.lshl(frame);
                break;
            case ISHR:
                Shr.ishr(frame);
                break;
            case LSHR:
                Shr.lshr(frame);
                break;
            case IUSHR:
                Ushr.iushr(frame);
                break;
            case LUSHR:
                Ushr.lushr(frame);
                break;
            case IAND:
                And.iand(frame);
                break;
            case LAND:
                And.land(frame);
                break;
            case IOR:
                Or.ior(frame);
                break;
            case LOR:
                Or.lor(frame);
                break;
            case IXOR:
                Xor.ixor(frame);
                break;
            case LXOR:
                Xor.lxor(frame);
                break;
            case IINC:
                Iinc.iinc(frame);
                break;
            case I2L:
                I2x.i2l(frame);
                break;
            case I2F:
                I2x.i2f(frame);
                break;
            case I2D:
                I2x.i2d(frame);
                break;
            case L2I:
                L2x.l2i(frame);
                break;
            case L2F:
                L2x.l2f(frame);
                break;
            case L2D:
                L2x.l2d(frame);
                break;
            case F2I:
                F2x.f2i(frame);
                break;
            case F2L:
                F2x.f2l(frame);
                break;
            case F2D:
                F2x.f2d(frame);
                break;
            case D2I:
                D2x.d2i(frame);
                break;
            case D2L:
                D2x.d2l(frame);
                break;
            case D2F:
                D2x.d2f(frame);
                break;
            case I2B:
                I2x.i2b(frame);
                break;
            case I2C:
                I2x.i2c(frame);
                break;
            case I2S:
                I2x.i2s(frame);
                break;
            case LCMP:
                Lcmp.lcmp(frame);
                break;
            case FCMPL:
                Fcmp.fcmpl(frame);
                break;
            case FCMPG:
                Fcmp.fcmpg(frame);
                break;
            case DCMPL:
                Dcmp.dcmpl(frame);
                break;
            case DCMPG:
                Dcmp.dcmpg(frame);
                break;
            case IFEQ:
                IfCmp.ifeq(frame);
                break;
            case IFNE:
                IfCmp.ifne(frame);
                break;
            case IFLT:
                IfCmp.iflt(frame);
                break;
            case IFGE:
                IfCmp.ifge(frame);
                break;
            case IFGT:
                IfCmp.ifgt(frame);
                break;
            case IFLE:
                IfCmp.ifle(frame);
                break;
            case IF_ICMPEQ:
                IfIcmp.if_icmpeq(frame);
                break;
            case IF_ICMPNE:
                IfIcmp.if_icmpne(frame);
                break;
            case IF_ICMPLT:
                IfIcmp.if_icmplt(frame);
                break;
            case IF_ICMPGE:
                IfIcmp.if_icmpge(frame);
                break;
            case IF_ICMPGT:
                IfIcmp.if_icmpgt(frame);
                break;
            case IF_ICMPLE:
                IfIcmp.if_icmple(frame);
                break;
            case IF_ACMPEQ:
                IfAcmp.if_acmpeq(frame);
                break;
            case IF_ACMPNE:
                IfAcmp.if_acmpne(frame);
                break;
            case GOTO:
                Goto.goto_(frame);
                break;
            case JSR:
                Jsr.jsr(frame);
                break;
            case RET:
                Ret.ret(frame);
                break;
            case TABLESWITCH:
                Tableswitch.tableswitch(frame);
                break;
            case LOOKUPSWITCH:
                Lookupswitch.lookupswitch(frame);
                break;
            case IRETURN:
                Return.ireturn(frame);
                break;
            case LRETURN:
                Return.lreturn(frame);
                break;
            case FRETURN:
                Return.freturn(frame);
                break;
            case DRETURN:
                Return.dreturn(frame);
                break;
            case ARETURN:
                Return.areturn(frame);
                break;
            case RETURN:
                Return.return_(frame);
                break;
            case GETSTATIC:
                Getstatic.getstatic(frame);
                break;
            case PUTSTATIC:
                Putstatic.putstatic(frame);
                break;
            case GETFIELD:
                Getfield.getfield(frame);
                break;
            case PUTFIELD:
                Putfield.putfield(frame);
                break;
            case INVOKEVIRTUAL:
                Invokevirtual.invokevirtual(frame);
                break;
            case INVOKESPECIAL:
                Invokespecial.invokespecial(frame);
                break;
            case INVOKESTATIC:
                Invokestatic.invokestatic(frame);
                break;
            case INVOKEINTERFACE:
                Invokeinterface.invokeinterface(frame);
                break;
            case INVOKEDYNAMIC:
                Invokedynamic.invokedynamic(frame);
                break;
            case NEW:
                New.new_(frame);
                break;
            case NEWARRAY:
                Newarray.newarray(frame);
                break;
            case ANEWARRAY:
                Anewarray.anewarray(frame);
                break;
            case ARRAYLENGTH:
                Arraylength.arraylength(frame);
                break;
            case ATHROW:
                Athrow.athrow(frame);
                break;
            case CHECKCAST:
                Checkcast.checkcast(frame);
                break;
            case INSTANCEOF:
                Instanceof.instanceof_(frame);
                break;
            case MONITORENTER:
                Monitorenter.monitorenter(frame);
                break;
            case MONITOREXIT:
                Monitorexit.monitorexit(frame);
                break;
            case MULTIANEWARRAY:
                Multianewarray.multianewarray(frame);
                break;
            case IFNULL:
                Ifnull.ifnull(frame);
                break;
            case IFNONNULL:
                Ifnonnull.ifnonnull(frame);
                break;
            default:
                frame.nonsupportOpCode();
                break;
        }
    }

    public int gasCost(Frame frame, OpCode opCode) {
        int gasCost = 1;
        switch (opCode) {
            case NOP:
                break;
            case ACONST_NULL:
            case ICONST_M1:
            case ICONST_0:
            case ICONST_1:
            case ICONST_2:
            case ICONST_3:
            case ICONST_4:
            case ICONST_5:
            case LCONST_0:
            case LCONST_1:
            case FCONST_0:
            case FCONST_1:
            case FCONST_2:
            case DCONST_0:
            case DCONST_1:
            case BIPUSH:
            case SIPUSH:
                gasCost = GasCost.CONSTANT;
                break;
            case LDC:
                Object value = frame.ldcInsnNode().cst;
                if (value instanceof Number) {
                    gasCost = GasCost.LDC;
                } else {
                    gasCost = Math.max(value.toString().length(), 1) * GasCost.LDC;
                }
                break;
            case ILOAD:
            case LLOAD:
            case FLOAD:
            case DLOAD:
            case ALOAD:
                gasCost = GasCost.LOAD;
                break;
            case IALOAD:
            case LALOAD:
            case FALOAD:
            case DALOAD:
            case AALOAD:
            case BALOAD:
            case CALOAD:
            case SALOAD:
                gasCost = GasCost.ARRAYLOAD;
                break;
            case ISTORE:
            case LSTORE:
            case FSTORE:
            case DSTORE:
            case ASTORE:
                gasCost = GasCost.STORE;
                break;
            case IASTORE:
            case LASTORE:
            case FASTORE:
            case DASTORE:
            case AASTORE:
            case BASTORE:
            case CASTORE:
            case SASTORE:
                gasCost = GasCost.ARRAYSTORE;
                break;
            case POP:
            case POP2:
            case DUP:
            case DUP_X1:
            case DUP_X2:
            case DUP2:
            case DUP2_X1:
            case DUP2_X2:
            case SWAP:
                gasCost = GasCost.STACK;
                break;
            case IADD:
            case LADD:
            case FADD:
            case DADD:
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB:
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL:
            case IDIV:
            case LDIV:
            case FDIV:
            case DDIV:
            case IREM:
            case LREM:
            case FREM:
            case DREM:
            case INEG:
            case LNEG:
            case FNEG:
            case DNEG:
            case ISHL:
            case LSHL:
            case ISHR:
            case LSHR:
            case IUSHR:
            case LUSHR:
            case IAND:
            case LAND:
            case IOR:
            case LOR:
            case IXOR:
            case LXOR:
            case IINC:
                gasCost = GasCost.MATH;
                break;
            case I2L:
            case I2F:
            case I2D:
            case L2I:
            case L2F:
            case L2D:
            case F2I:
            case F2L:
            case F2D:
            case D2I:
            case D2L:
            case D2F:
            case I2B:
            case I2C:
            case I2S:
                gasCost = GasCost.CONVERSION;
                break;
            case LCMP:
            case FCMPL:
            case FCMPG:
            case DCMPL:
            case DCMPG:
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
                gasCost = GasCost.COMPARISON;
                break;
            case GOTO:
            case JSR:
            case RET:
                gasCost = GasCost.CONTROL;
                break;
            case TABLESWITCH:
                TableSwitchInsnNode table = frame.tableSwitchInsnNode();
                gasCost = Math.max(table.max - table.min, 1) * GasCost.TABLESWITCH;
                break;
            case LOOKUPSWITCH:
                LookupSwitchInsnNode lookup = frame.lookupSwitchInsnNode();
                gasCost = Math.max(lookup.keys.size(), 1) * GasCost.LOOKUPSWITCH;
                break;
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
                gasCost = GasCost.CONTROL;
                break;
            case GETSTATIC:
            case PUTSTATIC:
            case GETFIELD:
            case PUTFIELD:
            case INVOKEVIRTUAL:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEINTERFACE:
            case INVOKEDYNAMIC:
            case NEW:
                gasCost = GasCost.REFERENCE;
                break;
            case NEWARRAY:
            case ANEWARRAY:
                int count = frame.operandStack.popInt();
                gasCost = Math.max(count, 1) * GasCost.NEWARRAY;
                frame.operandStack.pushInt(count);
                break;
            case ARRAYLENGTH:
            case ATHROW:
            case CHECKCAST:
            case INSTANCEOF:
            case MONITORENTER:
            case MONITOREXIT:
                gasCost = GasCost.REFERENCE;
                break;
            case MULTIANEWARRAY:
                MultiANewArrayInsnNode multiANewArrayInsnNode = frame.multiANewArrayInsnNode();
                int size = 1;
                int[] dimensions = new int[multiANewArrayInsnNode.dims];
                for (int i = multiANewArrayInsnNode.dims - 1; i >= 0; i--) {
                    int length = frame.operandStack.popInt();
                    if (length > 0) {
                        size *= length;
                    }
                    dimensions[i] = length;
                }
                for (int dimension : dimensions) {
                    frame.operandStack.pushInt(dimension);
                }
                gasCost = size * GasCost.MULTIANEWARRAY;
                break;
            case IFNULL:
            case IFNONNULL:
                gasCost = GasCost.EXTENDED;
                break;
            default:
                break;
        }
        return gasCost;
    }

    public BlockHeaderDto getBlockHeader(long number) {
        if (this.vmContext != null) {
            BlockHeaderDto blockHeader = null;
            try {
                if (number == programInvoke.getNumber() + 1) {
                    blockHeader = this.vmContext.getCurrentBlockHeader();
                } else {
                    blockHeader = this.vmContext.getBlockHeader(number);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (blockHeader == null) {
                log.error(String.format("blockHeader is null, number: %s", number));
            }
            return blockHeader;
        } else {
            throw new RuntimeException(String.format("vmContext is null, number: %s", number));
        }
    }

//    public BlockHeaderDto getBlockHeader(long number) {
//        BlockHeaderDto blockHeaderDto = new BlockHeaderDto();
//        blockHeaderDto.setHash("hash" + number);
//        blockHeaderDto.setHeight(number);
//        blockHeaderDto.setTxCount(100);
//        blockHeaderDto.setPackingAddress(AddressTool.getAddress("TTapY7gpBm1DHEgwguSFFtuK3JvGZVKK"));
//        blockHeaderDto.setTime(1535012808001L);
//        return blockHeaderDto;
//    }

    public Result getResult() {
        return result;
    }

    public String getResultString() {
        String result = null;
        Object resultValue = getResult().getValue();
        if (resultValue != null) {
            if (resultValue instanceof ObjectRef) {
                if (getResult().isError() || getResult().isException()) {
                    setResult(new Result());
                }
                result = this.heap.runToString((ObjectRef) resultValue);
            } else {
                result = resultValue.toString();
            }
        }
        return result;
    }

    public Object getResultValue() {
        return resultValue;
    }

    public VMContext getVmContext() {
        return vmContext;
    }

    public ProgramInvoke getProgramInvoke() {
        return programInvoke;
    }

    public ProgramContext getProgramContext() {
        return programContext;
    }

    public ProgramExecutor getProgramExecutor() {
        return programExecutor;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public long getGas() {
        return gas;
    }

    public long getGasLeft() {
        return gas - gasUsed;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public List<ProgramTransfer> getTransfers() {
        return transfers;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void setProgramExecutor(ProgramExecutor programExecutor) {
        this.programExecutor = programExecutor;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public void addGasUsed(long needGas) {
        long gasUsed = this.gasUsed + needGas;
        if (this.gas > 0 && gasUsed > this.gas) {
            this.gasUsed = this.gas;
            throw new ErrorException("not enough gas", this.gasUsed, null);
        } else {
            this.gasUsed = gasUsed;
        }
    }

    public void setGas(long gas) {
        this.gas = gas;
    }

}
