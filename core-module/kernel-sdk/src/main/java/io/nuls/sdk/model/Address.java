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

package io.nuls.sdk.model;

import io.nuls.sdk.constant.KernelErrorCode;
import io.nuls.sdk.constant.SDKConstant;
import io.nuls.sdk.crypto.Base58;
import io.nuls.sdk.exception.NulsRuntimeException;
import io.nuls.sdk.utils.AddressTool;
import io.nuls.sdk.utils.Log;
import io.nuls.sdk.utils.SerializeUtils;

import java.util.Arrays;

/**
 * @author: Chralie
 * @date: 2018/5/4
 */
public class Address {


    /**
     * hash length
     */
    public static final int ADDRESS_LENGTH = 23;

    /**
     * RIPEMD160 length
     */
    private static final int LENGTH = 20;

    /**
     * chain id
     */
    private short chainId = SDKConstant.DEFAULT_CHAIN_ID;

    /**
     * address type
     */
    private byte addressType;

    /**
     * hash160 of public key
     */
    protected byte[] hash160;

    /**
     * @param address bytes
     */

    protected byte[] addressBytes;

    /**
     * @param address
     */
    public Address(String address) {
        try {
            byte[] bytes = AddressTool.getAddress(address);

            Address addressTmp = Address.fromHashs(bytes);
            this.chainId = addressTmp.getChainId();
            this.addressType = addressTmp.getAddressType();
            this.hash160 = addressTmp.getHash160();
            this.addressBytes = calcAddressbytes();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public Address(short chainId, byte addressType, byte[] hash160) {
        this.chainId = chainId;
        this.addressType = addressType;
        this.hash160 = hash160;
        this.addressBytes = calcAddressbytes();
    }

    public byte[] getHash160() {
        return hash160;
    }

    public short getChainId() {
        return chainId;
    }

    public static Address fromHashs(String address) throws Exception {
        byte[] bytes = Base58.decode(address);
        return fromHashs(bytes);
    }

    public static Address fromHashs(byte[] hashs) {
        if (hashs == null || hashs.length != ADDRESS_LENGTH) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR);
        }

        short chainId = SerializeUtils.bytes2Short(hashs);
        byte addressType = hashs[2];
        byte[] content = new byte[LENGTH];
        System.arraycopy(hashs, 3, content, 0, LENGTH);

        Address address = new Address(chainId, addressType, content);
        return address;
    }

    public byte[] calcAddressbytes() {
        byte[] body = new byte[ADDRESS_LENGTH];
        System.arraycopy(SerializeUtils.shortToBytes(chainId), 0, body, 0, 2);
        body[2] = this.addressType;
        System.arraycopy(hash160, 0, body, 3, hash160.length);
        return body;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Address) {
            Address other = (Address) obj;
            return Arrays.equals(this.addressBytes, other.getAddressBytes());
        }
        return false;
    }

    public byte[] getAddressBytes() {
        return addressBytes;
    }

    public void setAddressBytes(byte[] addressBytes) {
        this.addressBytes = addressBytes;
    }

    public byte getAddressType() {
        return addressType;
    }

    public void setAddressType(byte addressType) {
        this.addressType = addressType;
    }

    public static int size() {
        return ADDRESS_LENGTH;
    }

    public String getBase58() {
        return AddressTool.getStringAddressByBytes(this.addressBytes);
    }

    @Override
    public String toString() {
        return AddressTool.getStringAddressByBytes(this.addressBytes);
    }

}
