/**
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
 */
package io.nuls.contract.entity.txdata;


import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.TransactionLogicData;
import io.nuls.kernel.utils.NulsByteBuffer;
import io.nuls.kernel.utils.NulsOutputStreamBuffer;
import io.nuls.kernel.utils.SerializeUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: PierreLuo
 */
public class CreateContractData extends TransactionLogicData implements ContractData{

    private byte[] sender;
    private byte[] contractAddress;
    private long value;
    private int codeLen;
    private byte[] code;
    private long gasLimit;
    private long price;
    private byte argsCount;
    private String[][] args;

    @Override
    public int size() {
        int size = 0;
        size += Address.ADDRESS_LENGTH;
        size += Address.ADDRESS_LENGTH;
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfBytes(code);
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt64();
        size += 1;
        if(args != null) {
            for(String[] arg : args) {
                if(arg == null) {
                    size += 1;
                } else {
                    size += 1;
                    for(String str : arg) {
                        size += SerializeUtils.sizeOfString(str);
                    }
                }
            }
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(sender);
        stream.write(contractAddress);
        stream.writeInt64(value);
        stream.writeUint32(codeLen);
        stream.writeBytesWithLength(code);
        stream.writeInt64(gasLimit);
        stream.writeInt64(price);
        stream.write(argsCount);
        if(args != null) {
            for(String[] arg : args) {
                if(arg == null) {
                    stream.write((byte) 0);
                } else {
                    stream.write((byte) arg.length);
                    for(String str : arg){
                        stream.writeString(str);
                    }
                }
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.sender = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.contractAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.value = byteBuffer.readInt64();
        this.codeLen = byteBuffer.readInt32();
        this.code = byteBuffer.readByLengthByte();
        this.gasLimit = byteBuffer.readInt64();
        this.price = byteBuffer.readInt64();
        this.argsCount = byteBuffer.readByte();
        byte length = this.argsCount;
        this.args = new String[length][];
        for(byte i = 0; i < length; i++) {
            byte argCount = byteBuffer.readByte();
            if(argCount == 0) {
                args[i] = new String[0];
            } else {
                String[] arg = new String[argCount];
                for(byte k = 0; k < argCount; k++) {
                    arg[k] = byteBuffer.readString();
                }
                args[i] = arg;
            }
        }
    }

    @Override
    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public byte[] getSender() {
        return sender;
    }

    public void setSender(byte[] sender) {
        this.sender = sender;
    }

    @Override
    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public int getCodeLen() {
        return codeLen;
    }

    public void setCodeLen(int codeLen) {
        this.codeLen = codeLen;
    }

    public byte[] getCode() {
        return code;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }

    @Override
    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    @Override
    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public byte getArgsCount() {
        return argsCount;
    }

    public void setArgsCount(byte argsCount) {
        this.argsCount = argsCount;
    }

    public String[][] getArgs() {
        return args;
    }

    public void setArgs(String[][] args) {
        this.args = args;
    }

    @Override
    public Set<byte[]> getAddresses() {
        Set<byte[]> addressSet = new HashSet<>();
        addressSet.add(contractAddress);
        return addressSet;
    }

}
