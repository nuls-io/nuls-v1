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

import io.nuls.contract.entity.BlockHeaderDto;
import io.nuls.contract.sdk.Block;
import io.nuls.contract.vm.Frame;
import io.nuls.contract.vm.MethodArgs;
import io.nuls.contract.vm.ObjectRef;
import io.nuls.contract.vm.Result;
import io.nuls.contract.vm.code.MethodCode;
import io.nuls.contract.vm.code.VariableType;
import io.nuls.contract.vm.natives.NativeMethod;
import org.spongycastle.util.encoders.Hex;

import static io.nuls.contract.vm.natives.NativeMethod.NOT_SUPPORT_NATIVE;
import static io.nuls.contract.vm.natives.NativeMethod.SUPPORT_NATIVE;

public class NativeBlock {

    public static final String TYPE = "io/nuls/contract/sdk/Block";

    public static Result nativeRun(MethodCode methodCode, MethodArgs methodArgs, Frame frame, boolean check) {
        switch (methodCode.fullName) {
            case getBlockHeader:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return getBlockHeader(methodCode, methodArgs, frame);
                }
            case currentBlockHeader:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return currentBlockHeader(methodCode, methodArgs, frame);
                }
            case newestBlockHeader:
                if (check) {
                    return SUPPORT_NATIVE;
                } else {
                    return newestBlockHeader(methodCode, methodArgs, frame);
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

    public static final String getBlockHeader = TYPE + "." + "getBlockHeader" + "(J)Lio/nuls/contract/sdk/BlockHeader;";

    /**
     * native
     *
     * @see Block#getBlockHeader(long)
     */
    private static Result getBlockHeader(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        long blockNumber = (long) methodArgs.invokeArgs[0];
        ObjectRef objectRef = getBlockHeader(blockNumber, frame);
        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    public static final String currentBlockHeader = TYPE + "." + "currentBlockHeader" + "()Lio/nuls/contract/sdk/BlockHeader;";

    /**
     * native
     *
     * @see Block#currentBlockHeader()
     */
    private static Result currentBlockHeader(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        long blockNumber = frame.vm.getProgramInvoke().getNumber();
        ObjectRef objectRef = getBlockHeader(blockNumber + 1, frame);
        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    public static final String newestBlockHeader = TYPE + "." + "newestBlockHeader" + "()Lio/nuls/contract/sdk/BlockHeader;";

    /**
     * native
     *
     * @see Block#newestBlockHeader()
     */
    private static Result newestBlockHeader(MethodCode methodCode, MethodArgs methodArgs, Frame frame) {
        long blockNumber = frame.vm.getProgramInvoke().getNumber();
        ObjectRef objectRef = getBlockHeader(blockNumber, frame);
        Result result = NativeMethod.result(methodCode, objectRef, frame);
        return result;
    }

    private static ObjectRef getBlockHeader(long blockNumber, Frame frame) {

        String fieldName = "BlockHeader$" + blockNumber;
        Object object = frame.heap.getStatic(VariableType.BLOCK_HEADER_TYPE.getType(), fieldName);
        if (object != null) {
            return (ObjectRef) object;
        }

        BlockHeaderDto blockHeaderDto = frame.vm.getBlockHeader(blockNumber);

        if (blockHeaderDto != null) {
            ObjectRef objectRef = frame.heap.newObject(VariableType.BLOCK_HEADER_TYPE);
            frame.heap.putField(objectRef, "hash", frame.heap.newString(blockHeaderDto.getHash()));
            frame.heap.putField(objectRef, "time", blockHeaderDto.getTime());
            frame.heap.putField(objectRef, "height", blockHeaderDto.getHeight());
            frame.heap.putField(objectRef, "txCount", blockHeaderDto.getTxCount());
            ObjectRef packingAddress = null;
            if (blockHeaderDto.getPackingAddress() != null) {
                packingAddress = frame.heap.newAddress(NativeAddress.toString(blockHeaderDto.getPackingAddress()));
            }
            frame.heap.putField(objectRef, "packingAddress", packingAddress);
            String stateRoot = null;
            if (blockHeaderDto.getStateRoot() != null) {
                stateRoot = Hex.toHexString(blockHeaderDto.getStateRoot());
            }
            frame.heap.putField(objectRef, "stateRoot", frame.heap.newString(stateRoot));
            frame.heap.putStatic(VariableType.BLOCK_HEADER_TYPE.getType(), fieldName, objectRef);
            return objectRef;
        }

        return null;
    }

}
