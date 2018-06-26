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

package io.nuls.kernel.utils;

import io.nuls.core.tools.array.ArraysTool;
import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.constant.KernelErrorCode;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.exception.NulsRuntimeException;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.script.P2PKHScriptSig;

/**
 * @author: Niels Wang
 * @date: 2018/5/9
 */
public class AddressTool {

    public static byte[] getAddress(String addressString) {
        byte[] bytes;
        try {
            bytes = Base58.decode(addressString);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(e);
        }
        byte[] result = new byte[Address.ADDRESS_LENGTH];
        System.arraycopy(bytes, 0, result, 0, Address.ADDRESS_LENGTH);
        return result;
    }

    public static byte[] getAddress(byte[] publicKey) {
        if (publicKey == null) {
            return null;
        }
        byte[] hash160 = SerializeUtils.sha256hash160(publicKey);
        Address address = new Address(NulsContext.DEFAULT_CHAIN_ID, NulsContext.DEFAULT_ADDRESS_TYPE, hash160);
        return address.getAddressBytes();
    }

    private static byte getXor(byte[] body) {
        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }
        return xor;
    }

    public static byte[] getAddress(P2PKHScriptSig scriptSig) {
        if (scriptSig == null) {
            return null;
        }
        return getAddress(scriptSig.getPublicKey());
    }

    public static boolean validAddress(String address) {
        if (StringUtils.isBlank(address)) {
            return false;
        }
        byte[] bytes;
        try {
            bytes = Base58.decode(address);
            if (bytes.length != Address.ADDRESS_LENGTH + 1) {
                return false;
            }
        } catch (NulsException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        try {
            checkXOR(bytes);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public static void checkXOR(byte[] hashs) {
        byte[] body = new byte[Address.ADDRESS_LENGTH];
        System.arraycopy(hashs, 0, body, 0, Address.ADDRESS_LENGTH);

        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }
        byte[] sign = new byte[1];
        System.arraycopy(hashs, Address.ADDRESS_LENGTH, sign, 0, 1);

        if (xor != hashs[Address.ADDRESS_LENGTH]) {
            throw new NulsRuntimeException(KernelErrorCode.DATA_ERROR);
        }
    }

    public static String getStringAddressByBytes(byte[] addressBytes) {
        byte[] bytes = ArraysTool.concatenate(addressBytes, new byte[]{getXor(addressBytes)});
        return Base58.encode(bytes);
    }


    public static boolean checkPublicKeyHash(byte[] address, byte[] pubKeyHash) {

        if (address == null || pubKeyHash == null) {
            return false;
        }
        int pubKeyHashLength = pubKeyHash.length;
        if (address.length != Address.ADDRESS_LENGTH || pubKeyHashLength != 20) {
            return false;
        }
        for (int i = 0; i < pubKeyHashLength; i++) {
            if (pubKeyHash[i] != address[i + 3]) {
                return false;
            }
        }
        return true;
    }

}
