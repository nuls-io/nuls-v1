/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.account.entity;

import io.nuls.core.exception.NulsException;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.crypto.Base58;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.crypto.Utils;
import io.nuls.core.utils.log.Log;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class Address {
    /**
     *
     */
    public static final int HASH_LENGTH = 23;

    // RIPEMD160 length
    public static final int LENGTH = 20;

    /*
    * hash160 of public key
    */
    protected byte[] hash160;

    /**
     * chain id
     */
    private short addressType = 0;

    public Address(String address) {
        try {
            byte[] bytes = Base58.decode(address);

            Address addressTmp = Address.fromHashs(bytes);
            this.addressType = addressTmp.getAddressType();
            this.hash160 = addressTmp.getHash160();
        } catch (NulsException e) {
            Log.error(e);
        }
    }

    public Address(short addressType, byte[] hash160) {
        this.addressType = addressType;
        this.hash160 = hash160;
    }

    private byte[] getHash160() {
        return hash160;
    }

    @Override
    public String toString() {
        return getBase58();
    }

    public short getAddressType() {
        return this.addressType;
    }

    public String getBase58() {
        return Base58.encode(getHash());
    }

    public static Address fromHashs(String address) throws NulsException {
        byte[] bytes = Base58.decode(address);
        return fromHashs(bytes);
    }

    public static Address fromHashs(byte[] hashs) throws NulsException {
        if (hashs == null || hashs.length != HASH_LENGTH) {
            throw new NulsException(ErrorCode.DATA_ERROR);
        }

        short addressType = Utils.bytes2Short(hashs);
        byte[] content = new byte[LENGTH];
        System.arraycopy(hashs, 2, content, 0, LENGTH);

        byte[] sign = new byte[1];
        System.arraycopy(hashs, 22, sign, 0, 1);

        Address address = new Address(addressType, content);
        address.checkXOR(sign[0]);
        return address;
    }

    public byte[] getHash() {
        byte[] body = new byte[22];
        System.arraycopy(Utils.shortToBytes(addressType), 0, body, 0, 2);
        System.arraycopy(hash160, 0, body, 2, hash160.length);
        byte xor = getXor(body);
        byte[] base58bytes = new byte[23];
        System.arraycopy(body, 0, base58bytes, 0, body.length);
        base58bytes[body.length] = xor;
        return base58bytes;
    }

    protected byte getXor(byte[] body) {

        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }

        return xor;
    }

    protected void checkXOR(byte xorByte) throws NulsException {
        byte[] body = new byte[22];
        System.arraycopy(Utils.shortToBytes(addressType), 0, body, 0, 2);
        System.arraycopy(hash160, 0, body, 2, hash160.length);

        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }

        if (xor != xorByte) {
            throw new NulsException(ErrorCode.DATA_ERROR);
        }
    }

    @Override
    public boolean equals(Object obj) {
        Address other = (Address) obj;
        return this.getBase58().equals(other.getBase58());
    }

    public String hashHex() {
        return Hex.encode(getHash());
    }

}
