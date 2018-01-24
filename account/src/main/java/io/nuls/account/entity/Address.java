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
package io.nuls.account.entity;

import io.nuls.core.crypto.Sha256Hash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.utils.crypto.Base58;
import io.nuls.core.utils.crypto.Hex;
import io.nuls.core.utils.log.Log;

import java.util.Arrays;

/**
 * @author Niels
 * @date 2017/10/30
 */
public class Address {
    //base58 length
    public static final int HASH_LENGTH = 25;
    // RIPEMD160 length
    public static final int LENGTH = 20;
    //content
    protected byte[] hash160;

    //chain id
    private int chainId = 0;

    public Address(String address) {
        try {
            byte[] bytes = Base58.decode(address);

            Address addressTmp = Address.fromHashs(bytes);
            this.chainId = addressTmp.getChainId();
            this.hash160 = addressTmp.getHash160();
        } catch (NulsException e) {
            Log.error(e);
        }
    }


    public Address(Integer chainId, byte[] hash160) {
        this.chainId = chainId;
        this.hash160 = hash160;
    }


    public byte[] getHash160() {
        return hash160;
    }

    @Override
    public String toString() {
        return getBase58();
    }

    public int getChainId() {
        return this.chainId;
    }

    public String getBase58() {
        return Base58.encode(getHash());
    }

    public String getCommonBase58() {
        return Base58.encode(hash160);
    }

    public static Address fromHashs(String address) throws NulsException {
        byte[] bytes = Base58.decode(address);
        return fromHashs(bytes);
    }

    public static Address fromHashs(byte[] hashs) throws NulsException {
        if (hashs == null || hashs.length != HASH_LENGTH) {
            throw new NulsException(ErrorCode.DATA_ERROR);
        }

        int chainId = hashs[0] & 0XFF;
        byte[] content = new byte[LENGTH];
        System.arraycopy(hashs, 1, content, 0, LENGTH);

        byte[] sign = new byte[4];
        System.arraycopy(hashs, 21, sign, 0, 4);

        Address address = new Address(chainId, content);
        address.checkSign(sign);
        return address;
    }

    public byte[] getHash() {
        byte[] versionAndHash160 = new byte[21];
        versionAndHash160[0] = (byte) chainId;
        System.arraycopy(hash160, 0, versionAndHash160, 1, hash160.length);
        byte[] checkSin = getCheckSign(versionAndHash160);
        byte[] base58bytes = new byte[25];
        System.arraycopy(versionAndHash160, 0, base58bytes, 0, versionAndHash160.length);
        System.arraycopy(checkSin, 0, base58bytes, versionAndHash160.length, checkSin.length);
        return base58bytes;
    }

    protected byte[] getCheckSign(byte[] versionAndHash160) {
        byte[] checkSin = new byte[4];
        System.arraycopy(Sha256Hash.hashTwice(versionAndHash160), 0, checkSin, 0, 4);
        return checkSin;
    }

    protected void checkSign(byte[] sign) throws NulsException {

        byte[] versionAndHash160 = new byte[21];
        versionAndHash160[0] = (byte) chainId;
        System.arraycopy(hash160, 0, versionAndHash160, 1, hash160.length);
        byte[] checkSin = new byte[4];
        System.arraycopy(Sha256Hash.hashTwice(versionAndHash160), 0, checkSin, 0, 4);

        if (!Arrays.equals(checkSin, sign)) {
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
