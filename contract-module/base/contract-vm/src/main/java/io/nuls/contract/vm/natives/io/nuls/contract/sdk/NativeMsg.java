package io.nuls.contract.vm.natives.io.nuls.contract.sdk;

import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.natives.NativeMethod;

public class NativeMsg {

    public static final String TYPE = "io/nuls/contract/sdk/Msg";

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.name) {
            case "gasleft":
                result = gasleft(methodCode, methodArgs, frame);
                break;
            case "sender":
                result = sender(methodCode, methodArgs, frame);
                break;
            case "value":
                result = value(methodCode, methodArgs, frame);
                break;
            case "gasprice":
                result = gasprice(methodCode, methodArgs, frame);
                break;
            case "address":
                result = address(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result gasleft(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getGasLeft(), frame);
        return result;
    }

    private static Result sender(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getProgramContext().getSender(), frame);
        return result;
    }

    private static Result value(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getProgramContext().getValue(), frame);
        return result;
    }

    private static Result gasprice(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getProgramContext().getGasPrice(), frame);
        return result;
    }

    private static Result address(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = NativeMethod.result(methodCode, frame.vm.getProgramContext().getAddress(), frame);
        return result;
    }

}
