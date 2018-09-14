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
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.NulsDigestData;
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
public class ContractTransferData extends TransactionLogicData implements ContractData{

    private NulsDigestData orginTxHash;
    private byte[] contractAddress;
    private byte success;

    public ContractTransferData(){

    }

    public ContractTransferData(NulsDigestData orginTxHash, byte[] contractAddress, byte success) {
        this.orginTxHash = orginTxHash;
        this.contractAddress = contractAddress;
        this.success = success;
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(orginTxHash);
        size += Address.ADDRESS_LENGTH;
        size += 1;
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(orginTxHash);
        stream.write(contractAddress);
        stream.write(success);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.orginTxHash = byteBuffer.readHash();
        this.contractAddress = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        this.success = byteBuffer.readByte();
    }

    @Override
    public Set<byte[]> getAddresses() {
        Set<byte[]> addressSet = new HashSet<>();
        addressSet.add(contractAddress);
        return addressSet;
    }

    public byte getSuccess() {
        return success;
    }

    public void setSuccess(byte success) {
        this.success = success;
    }

    @Override
    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public NulsDigestData getOrginTxHash() {
        return orginTxHash;
    }

    public void setOrginTxHash(NulsDigestData orginTxHash) {
        this.orginTxHash = orginTxHash;
    }

    @Override
    public long getGasLimit() {
        return 0L;
    }

    @Override
    public byte[] getSender() {
        return null;
    }

    @Override
    public long getPrice() {
        return 0L;
    }
}
