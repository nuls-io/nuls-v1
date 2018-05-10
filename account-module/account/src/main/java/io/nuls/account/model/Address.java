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

package io.nuls.account.model;

import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.crypto.Utils;
import io.nuls.core.tools.log.Log;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.utils.AddressTool;
import io.protostuff.Tag;

/**
 * @author: Chralie
 * @date: 2018/5/4
 */
public class Address {
    /**
     *  hash length
     */
    public static final int HASH_LENGTH = 23;

    /**
     * RIPEMD160 length
     */
    public static final int LENGTH = 20;

    /**
     * chain id
     */

    private short chainId = NulsContext.DEFAULT_CHAIN_ID;

    /**
     * hash160 of public key
     */
    protected byte[] hash160;

    /**
     *
     * @param address bytes
     */
    @Tag(1)
    protected byte[] base58Bytes;

    /**
     * @param address
     */
    public Address(String address) {
        try {
            byte[] bytes = Base58.decode(address);

            Address addressTmp = Address.fromHashs(bytes);
            this.chainId = addressTmp.getChainId();
            this.hash160 = addressTmp.getHash160();
            this.base58Bytes = calcBase58bytes();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    public Address(short chainId, byte[] hash160) {
        this.chainId = chainId;
        this.hash160 = hash160;
        this.base58Bytes = calcBase58bytes();
    }

    public byte[] getHash160() {
        return hash160;
    }

    @Override
    public String toString() {
        return getBase58();
    }

    public short getChainId() {
        return chainId;
    }
    public String getBase58() {
        return Base58.encode(calcBase58bytes());
    }

    public static Address fromHashs(String address) throws Exception {
        byte[] bytes = Base58.decode(address);
        return fromHashs(bytes);
    }

    public static Address fromHashs(byte[] hashs) {
        if (hashs == null || hashs.length != HASH_LENGTH) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR);
        }

        short chainId = Utils.bytes2Short(hashs);
        byte[] content = new byte[LENGTH];
        System.arraycopy(hashs, 2, content, 0, LENGTH);

        Address address = new Address(chainId, content);
        AddressTool.checkXOR(address.calcBase58bytes());
        return address;
    }

    public byte[] calcBase58bytes() {
        byte[] body = new byte[22];
        System.arraycopy(Utils.shortToBytes(chainId), 0, body, 0, 2);
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

    public static boolean validAddress(String address) {
        return AddressTool.validAddress(address);
    }

    @Override
    public boolean equals(Object obj) {
        Address other = (Address) obj;
        return this.getBase58().equals(other.getBase58());
    }

    public String hashHex() {
        return Hex.encode(calcBase58bytes());
    }

    public byte[] getBase58Bytes() {
        return base58Bytes;
    }

    public void setBase58Bytes(byte[] base58Bytes) {
        this.base58Bytes = base58Bytes;
    }
}
