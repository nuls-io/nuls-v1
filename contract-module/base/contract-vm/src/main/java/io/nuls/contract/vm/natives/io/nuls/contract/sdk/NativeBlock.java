package io.nuls.contract.vm.natives.io.nuls.contract.sdk;

import io.nuls.contract.entity.BlockHeaderDto;
import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.natives.NativeMethod;
import org.spongycastle.util.encoders.Hex;

public class NativeBlock {

    public static final String TYPE = "io/nuls/contract/sdk/Block";

    public static Result run(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        Result result = null;
        switch (methodCode.getName()) {
            case "getBlockHeader":
                result = getBlockHeader(methodCode, methodArgs, frame);
                break;
            case "currentBlockHeader":
                result = currentBlockHeader(methodCode, methodArgs, frame);
                break;
            default:
                frame.nonsupportMethod(methodCode);
                break;
        }
        return result;
    }

    private static Result getBlockHeader(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        long blockNumber = (long) methodArgs.getInvokeArgs()[0];
        ObjectRef objectRef = getBlockHeader(blockNumber, frame);
        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    private static Result currentBlockHeader(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        long blockNumber = frame.getVm().getProgramContext().getNumber();
        ObjectRef objectRef = getBlockHeader(blockNumber, frame);
        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    private static ObjectRef getBlockHeader(long blockNumber, Frame frame) {

        String fieldName = "BlockHeader$" + blockNumber;
        Object object = frame.getHeap().getStatic(VariableType.BLOCK_HEADER_TYPE.getType(), fieldName);
        if (object != null) {
            return (ObjectRef) object;
        }

        BlockHeaderDto blockHeaderDto = frame.getVm().getBlockHeader(blockNumber);

        if (blockHeaderDto != null) {
            ObjectRef objectRef = frame.getHeap().newObject(VariableType.BLOCK_HEADER_TYPE);
            frame.getHeap().putField(objectRef, "hash", frame.getHeap().newString(blockHeaderDto.getHash()));
            frame.getHeap().putField(objectRef, "time", blockHeaderDto.getTime());
            frame.getHeap().putField(objectRef, "height", blockHeaderDto.getHeight());
            ObjectRef packingAddress = frame.getHeap().newAddress(NativeAddress.toString(blockHeaderDto.getPackingAddress()));
            frame.getHeap().putField(objectRef, "packingAddress", packingAddress);
            String stateRoot = null;
            if (blockHeaderDto.getStateRoot() != null) {
                stateRoot = Hex.toHexString(blockHeaderDto.getStateRoot());
            }
            frame.getHeap().putField(objectRef, "stateRoot", frame.getHeap().newString(stateRoot));
            frame.getHeap().putStatic(VariableType.BLOCK_HEADER_TYPE.getType(), fieldName, objectRef);
            return objectRef;
        }

        return null;
    }

}
