/**
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
 */
package io.nuls.contract.entity.txdata;


import io.nuls.kernel.exception.NulsException;
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
        size += SerializeUtils.sizeOfBytes(sender);
        size += SerializeUtils.sizeOfBytes(contractAddress);
        size += SerializeUtils.sizeOfVarInt(value);
        size += SerializeUtils.sizeOfVarInt(codeLen);
        size += SerializeUtils.sizeOfBytes(code);
        size += SerializeUtils.sizeOfVarInt(gasLimit);
        size += SerializeUtils.sizeOfVarInt(price);
        size += 1;
        if(args != null) {
            for(String[] arg : args) {
                size += 1;
                for(String str : arg) {
                    size += SerializeUtils.sizeOfString(str);
                }
            }
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(sender);
        stream.writeBytesWithLength(contractAddress);
        stream.writeVarInt(value);
        stream.writeVarInt(codeLen);
        stream.writeBytesWithLength(code);
        stream.writeVarInt(gasLimit);
        stream.writeVarInt(price);
        stream.write(argsCount);
        if(args != null) {
            for(String[] arg : args) {
                stream.write((byte) arg.length);
                for(String str : arg){
                    stream.writeString(str);
                }
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.sender = byteBuffer.readByLengthByte();
        this.contractAddress = byteBuffer.readByLengthByte();
        this.value = (long) byteBuffer.readVarInt();
        this.codeLen = (int) byteBuffer.readVarInt();
        this.code = byteBuffer.readByLengthByte();
        this.gasLimit = (long) byteBuffer.readVarInt();
        this.price = (long) byteBuffer.readVarInt();
        this.argsCount = byteBuffer.readByte();
        byte length = this.argsCount;
        this.args = new String[length][];
        for(byte i = 0; i < length; i++) {
            byte argCount = byteBuffer.readByte();
            String[] arg = new String[argCount];
            for(byte k = 0; k < argCount; k++) {
                arg[k] = byteBuffer.readString();
            }
            args[i] = arg;
        }
    }

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
