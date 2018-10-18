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
package io.nuls.contract.vm.natives.io.nuls.contract.sdk;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.vm.*;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.exception.ErrorException;
import io.nuls.contract.vm.exception.RevertException;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.program.ProgramCall;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.contract.vm.program.impl.ProgramInvoke;
import io.nuls.kernel.utils.AddressTool;

import java.math.BigInteger;
import java.util.Arrays;

import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeAddress {

    public static final String TYPE = "io/nuls/contract/sdk/Address";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case balance:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return balance(methodCode, methodArgs, frame);
                }
            case transfer:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return transfer(methodCode, methodArgs, frame);
                }
            case call:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return call(methodCode, methodArgs, frame);
                }
            case callWithReturnValue:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return callWithReturnValue(methodCode, methodArgs, frame);
                }
            case valid:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return valid(methodCode, methodArgs, frame);
                }
            default:
                if (check) {
                    return NOT_SUPPORT_NATIVE;
                } else {
                    frame.nonsupportMethod(methodCode);
                    return null;
                }
        }
    }

    private static BigInteger balance(byte[] address, Frame frame) {
        if (!frame.vm.getRepository().isExist(address)) {
            return BigInteger.ZERO;
        } else {
            return frame.vm.getProgramExecutor().getAccount(address).getBalance();
        }
    }

    public static final String balance = TYPE + "." + "balance" + "()Ljava/math/BigInteger;";

    /**
     * native
     *
     * @see Address#balance()
     */
    private static Result balance(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.objectRef;
        String address = frame.heap.runToString(objectRef);
        BigInteger balance = balance(NativeAddress.toBytes(address), frame);
        ObjectRef balanceRef = frame.heap.newBigInteger(balance.toString());
        Result result = NativeMethod.result(methodCode, balanceRef, frame);
        return result;
    }

    public static final String transfer = TYPE + "." + "transfer" + "(Ljava/math/BigInteger;)V";

    /**
     * native
     *
     * @see Address#transfer(BigInteger)
     */
    private static Result transfer(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef addressRef = methodArgs.objectRef;
        ObjectRef valueRef = (ObjectRef) methodArgs.invokeArgs[0];
        String address = frame.heap.runToString(addressRef);
        BigInteger value = frame.heap.toBigInteger(valueRef);
        byte[] from = frame.vm.getProgramInvoke().getContractAddress();
        byte[] to = NativeAddress.toBytes(address);
        if (Arrays.equals(from, to)) {
            throw new ErrorException(String.format("Cannot transfer from %s to %s", NativeAddress.toString(from), address), frame.vm.getGasUsed(), null);
        }
        checkBalance(from, value, frame);

        frame.vm.addGasUsed(GasCost.TRANSFER);

        if (frame.heap.existContract(to)) {
            //String address;
            String methodName = "_payable";
            String methodDesc = "()V";
            String[][] args = null;
            //BigInteger value;
            call(address, methodName, methodDesc, args, value, frame);
        } else {
            frame.vm.getProgramExecutor().getAccount(from).addBalance(value.negate());
            ProgramTransfer programTransfer = new ProgramTransfer(from, to, value);
            frame.vm.getTransfers().add(programTransfer);
        }

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static final String call = TYPE + "." + "call" + "(Ljava/lang/String;Ljava/lang/String;[[Ljava/lang/String;Ljava/math/BigInteger;)V";

    /**
     * native
     *
     * @see Address#call(String, String, String[][], BigInteger)
     */
    private static Result call(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        return call(methodCode, methodArgs, frame, false);
    }

    private static Result call(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean returnResult) {
        ObjectRef addressRef = methodArgs.objectRef;
        ObjectRef methodNameRef = (ObjectRef) methodArgs.invokeArgs[0];
        ObjectRef methodDescRef = (ObjectRef) methodArgs.invokeArgs[1];
        ObjectRef argsRef = (ObjectRef) methodArgs.invokeArgs[2];
        ObjectRef valueRef = (ObjectRef) methodArgs.invokeArgs[3];

        String address = frame.heap.runToString(addressRef);
        String methodName = frame.heap.runToString(methodNameRef);
        String methodDesc = frame.heap.runToString(methodDescRef);
        String[][] args = getArgs(argsRef, frame);
        BigInteger value = frame.heap.toBigInteger(valueRef);
        if (value == null) {
            value = BigInteger.ZERO;
        }

        String callResult = call(address, methodName, methodDesc, args, value, frame);
        Object resultValue = null;
        if (returnResult) {
            resultValue = frame.heap.newString(callResult);
        }

        Result result = NativeMethod.result(methodCode, resultValue, frame);
        return result;
    }

    public static final String callWithReturnValue = TYPE + "." + "callWithReturnValue" + "(Ljava/lang/String;Ljava/lang/String;[[Ljava/lang/String;Ljava/math/BigInteger;)Ljava/lang/String;";

    private static Result callWithReturnValue(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        return call(methodCode, methodArgs, frame, true);
    }

    private static String[][] getArgs(ObjectRef argsRef, Frame frame) {
        if (argsRef == null) {
            return null;
        }

        int length = argsRef.getDimensions()[0];
        String[][] array = new String[length][0];
        for (int i = 0; i < length; i++) {
            ObjectRef objectRef = (ObjectRef) frame.heap.getArray(argsRef, i);
            String[] ss = (String[]) frame.heap.getObject(objectRef);
            array[i] = ss;
        }

        return array;
    }

    private static String call(String address, String methodName, String methodDesc, String[][] args, BigInteger value, Frame frame) {
        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new ErrorException(String.format("amount less than zero, value=%s", value), frame.vm.getGasUsed(), null);
        }

        ProgramInvoke programInvoke = frame.vm.getProgramInvoke();
        ProgramCall programCall = new ProgramCall();
        programCall.setNumber(programInvoke.getNumber());
        programCall.setSender(programInvoke.getContractAddress());
        programCall.setValue(value != null ? value : BigInteger.ZERO);
        programCall.setGasLimit(programInvoke.getGasLimit() - frame.vm.getGasUsed());
        programCall.setPrice(programInvoke.getPrice());
        programCall.setContractAddress(NativeAddress.toBytes(address));
        programCall.setMethodName(methodName);
        programCall.setMethodDesc(methodDesc);
        programCall.setArgs(args);
        programCall.setEstimateGas(programInvoke.isEstimateGas());
        programCall.setInternalCall(true);

        if (programCall.getValue().compareTo(BigInteger.ZERO) > 0) {
            checkBalance(programCall.getSender(), programCall.getValue(), frame);
            frame.vm.getProgramExecutor().getAccount(programCall.getSender()).addBalance(programCall.getValue().negate());
            ProgramTransfer programTransfer = new ProgramTransfer(programCall.getSender(), programCall.getContractAddress(), programCall.getValue());
            frame.vm.getTransfers().add(programTransfer);
        }

        ProgramResult programResult = frame.vm.getProgramExecutor().call(programCall);

        frame.vm.addGasUsed(programResult.getGasUsed());
        if (programResult.isSuccess()) {
            frame.vm.getTransfers().addAll(programResult.getTransfers());
            frame.vm.getEvents().addAll(programResult.getEvents());
            return programResult.getResult();
        } else if (programResult.isError()) {
            throw new ErrorException(programResult.getErrorMessage(), programResult.getGasUsed(), programResult.getStackTrace());
        } else if (programResult.isRevert()) {
            throw new RevertException(programResult.getErrorMessage(), programResult.getStackTrace());
        } else {
            throw new RuntimeException("error contract status");
        }

    }

    private static void checkBalance(byte[] address, BigInteger value, Frame frame) {
        if (value == null || value.compareTo(BigInteger.ZERO) <= 0) {
            throw new ErrorException(String.format("transfer amount error, value=%s", value), frame.vm.getGasUsed(), null);
        }
        BigInteger balance = frame.vm.getProgramExecutor().getAccount(address).getBalance();
        if (balance.compareTo(value) < 0) {
            if (frame.vm.getProgramContext().isEstimateGas()) {
                balance = value;
            } else {
                throw new ErrorException(String.format("contract[%s] not enough balance", toString(address)), frame.vm.getGasUsed(), null);
            }
        }
    }

    public static final String valid = TYPE + "." + "valid" + "(Ljava/lang/String;)V";

    /**
     * native
     *
     * @see Address#valid(String)
     */
    private static Result valid(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.invokeArgs[0];
        String str = frame.heap.runToString(objectRef);
        boolean valided = validAddress(str);
        if (!valided) {
            frame.throwRuntimeException(String.format("address[%s] error", str));
        }
        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    public static String toString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try {
            return AddressTool.getStringAddressByBytes(bytes);
        } catch (Exception e) {
            throw new RuntimeException("address error", e);
        }
    }

    public static byte[] toBytes(String str) {
        if (str == null) {
            return null;
        }
        try {
            return AddressTool.getAddress(str);
        } catch (Exception e) {
            throw new RuntimeException("address error", e);
        }
    }

    public static boolean validAddress(String str) {
        return AddressTool.validAddress(str);
    }

}
