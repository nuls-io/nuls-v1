package io.nuls.contract.vm.natives.io.nuls.contract.sdk;

import io.nuls.contract.vm.*;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;
import io.nuls.contract.vm.program.ProgramCall;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.contract.vm.program.impl.ProgramInvoke;
import io.nuls.kernel.utils.AddressTool;

import java.math.BigInteger;
import java.util.Arrays;

public class NativeAddress {

    public static final String TYPE = "io/nuls/contract/sdk/Address";

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.getName()) {
            case "balance":
                result = balance(methodCode, methodArgs, frame);
                break;
            case "transfer":
                result = transfer(methodCode, methodArgs, frame);
                break;
            case "call":
                result = call(methodCode, methodArgs, frame);
                break;
//            case "toString":
//                result = toString(methodCode, methodArgs, frame);
//                break;
//            case "toBytes":
//                result = toBytes(methodCode, methodArgs, frame);
//                break;
            case "valid":
                result = valid(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static BigInteger balance(byte[] address, Frame frame) {
        return frame.getVm().getRepository().getBalance(address);
    }

    private static Result balance(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = methodArgs.getObjectRef();
        String address = frame.getHeap().runToString(objectRef);
        BigInteger balance = balance(NativeAddress.toBytes(address), frame);
        ObjectRef balanceRef = frame.getHeap().newBigInteger(balance.toString());
        Result result = NativeMethod.result(methodCode, balanceRef, frame);
        return result;
    }

    private static Result transfer(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef addressRef = methodArgs.getObjectRef();
        ObjectRef valueRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
        String address = frame.getHeap().runToString(addressRef);
        BigInteger value = frame.getHeap().toBigInteger(valueRef);
        byte[] from = frame.getVm().getProgramInvoke().getContractAddress();
        byte[] to = NativeAddress.toBytes(address);
        if (Arrays.equals(from, to)) {
            throw new ErrorException(String.format("Cannot transfer from %s to %s", NativeAddress.toString(from), address), frame.getVm().getGasUsed(), null);
        }
        if (value == null || value.compareTo(BigInteger.ZERO) <= 0) {
            throw new ErrorException(String.format("transfer amount error, value=%s", value), frame.getVm().getGasUsed(), null);
        }
        BigInteger balance = balance(from, frame);
        if (balance.compareTo(value) < 0) {
            if (frame.getVm().getProgramContext().isEstimateGas()) {
                balance = value;
            } else {
                throw new ErrorException("Not enough balance", frame.getVm().getGasUsed(), null);
            }
        }

        frame.getVm().addGasUsed(GasCost.TRANSFER);

        if (frame.getHeap().existContract(to)) {
            //String address;
            String methodName = "_payable";
            String methodDesc = "()V";
            String[][] args = null;
            //BigInteger value;
            call(address, methodName, methodDesc, args, value, frame);
        } else {
            frame.getVm().getRepository().addBalance(from, value.negate());
            ProgramTransfer programTransfer = new ProgramTransfer(from, to, value);
            frame.getVm().getTransfers().add(programTransfer);
        }

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    private static Result call(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef addressRef = methodArgs.getObjectRef();
        ObjectRef methodNameRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
        ObjectRef methodDescRef = (ObjectRef) methodArgs.getInvokeArgs()[1];
        ObjectRef argsRef = (ObjectRef) methodArgs.getInvokeArgs()[2];
        ObjectRef valueRef = (ObjectRef) methodArgs.getInvokeArgs()[3];

        String address = frame.getHeap().runToString(addressRef);
        String methodName = frame.getHeap().runToString(methodNameRef);
        String methodDesc = frame.getHeap().runToString(methodDescRef);
        String[][] args = getArgs(argsRef, frame);
        BigInteger value = frame.getHeap().toBigInteger(valueRef);
        if (value == null) {
            value = BigInteger.ZERO;
        }

        call(address, methodName, methodDesc, args, value, frame);

        Result result = NativeMethod.result(methodCode, null, frame);
        return result;
    }

    private static String[][] getArgs(ObjectRef argsRef, Frame frame) {
        if (argsRef == null) {
            return null;
        }

        int length = argsRef.getDimensions()[0];
        String[][] array = new String[length][0];
        for (int i = 0; i < length; i++) {
            ObjectRef objectRef = (ObjectRef) frame.getHeap().getArray(argsRef, i);
            String[] ss = (String[]) frame.getHeap().getObject(objectRef);
            array[i] = ss;
        }

        return array;
    }

    private static void call(String address, String methodName, String methodDesc, String[][] args, BigInteger value, Frame frame) {
        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new ErrorException(String.format("amount less than zero, value=%s", value), frame.getVm().getGasUsed(), null);
        }

        ProgramInvoke programInvoke = frame.getVm().getProgramInvoke();
        ProgramCall programCall = new ProgramCall();
        programCall.setNumber(programInvoke.getNumber());
        programCall.setSender(programInvoke.getContractAddress());
        programCall.setValue(value != null ? value : BigInteger.ZERO);
        programCall.setGasLimit(programInvoke.getGasLimit() - frame.getVm().getGasUsed());
        programCall.setPrice(programInvoke.getPrice());
        programCall.setContractAddress(NativeAddress.toBytes(address));
        programCall.setMethodName(methodName);
        programCall.setMethodDesc(methodDesc);
        programCall.setArgs(args);
        programCall.setEstimateGas(programInvoke.isEstimateGas());

        if (programCall.getValue().compareTo(BigInteger.ZERO) > 0) {
            frame.getVm().getRepository().addBalance(programCall.getSender(), programCall.getValue().negate());
            ProgramTransfer programTransfer = new ProgramTransfer(programCall.getSender(), programCall.getContractAddress(), programCall.getValue());
            frame.getVm().getTransfers().add(programTransfer);
        }

        ProgramResult programResult = frame.getVm().getProgramExecutor().call(programCall);

        frame.getVm().addGasUsed(programResult.getGasUsed());
        if (programResult.isSuccess()) {
            frame.getVm().getTransfers().addAll(programResult.getTransfers());
            frame.getVm().getEvents().addAll(programResult.getEvents());
        } else if (programResult.isError()) {
            throw new ErrorException(programResult.getErrorMessage(), programResult.getGasUsed(), programResult.getStackTrace());
        } else if (programResult.isRevert()) {
            throw new RevertException(programResult.getErrorMessage(), programResult.getStackTrace());
        } else {
            throw new RuntimeException("error contract status");
        }

    }

//    private static Result toString(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
//        ObjectRef objectRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
//        byte[] bytes = (byte[]) frame.getHeap().getObject(objectRef);
//        String str = toString(bytes);
//        ObjectRef ref = frame.getHeap().newString(str);
//        Result result = NativeMethod.result(methodCode, ref, frame);
//        return result;
//    }
//
//    private static Result toBytes(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
//        ObjectRef objectRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
//        String str = (String) frame.getHeap().getObject(objectRef);
//        byte[] bytes = toBytes(str);
//        ObjectRef ref = frame.getHeap().newArray(bytes);
//        Result result = NativeMethod.result(methodCode, ref, frame);
//        return result;
//    }

    private static Result valid(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        ObjectRef objectRef = (ObjectRef) methodArgs.getInvokeArgs()[0];
        String str = frame.getHeap().runToString(objectRef);
        boolean valided = validAddress(str);
        if (!valided) {
            frame.throwRuntimeException("address error");
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
