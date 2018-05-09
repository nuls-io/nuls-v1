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

import io.nuls.core.tools.crypto.Base58;
import io.nuls.core.tools.crypto.Utils;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.script.P2PKHScriptSig;

/**
 * @author: Niels Wang
 * @date: 2018/5/9
 */
public class AddressTool {

    public static byte[] getAddress(byte[] publicKey) {
        if(publicKey == null) {
            return null;
        }
        byte[] hash160 = Utils.sha256hash160(publicKey);
        short chainId = NulsContext.DEFAULT_CHAIN_ID;
        byte[] body = new byte[22];
        System.arraycopy(Utils.shortToBytes(chainId), 0, body, 0, 2);
        System.arraycopy(hash160, 0, body, 2, hash160.length);
        byte xor = getXor(body);
        byte[] base58bytes = new byte[23];
        System.arraycopy(body, 0, base58bytes, 0, body.length);
        base58bytes[body.length] = xor;
        return base58bytes;
    }

    private static byte getXor(byte[] body) {
        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }

        return xor;
    }

    public static byte[] getAddress(P2PKHScriptSig scriptSig) {
        if(scriptSig == null) {
            return null;
        }
        return getAddress(scriptSig.getPublicKey());
    }

    public static String getAddressBase58(byte[] publicKey) {
        if(publicKey == null) {
            return null;
        }
        byte[] bytes = getAddress(publicKey);
        return Base58.encode(bytes);
    }

    public static String getAddressBase58(P2PKHScriptSig scriptSig) {
        if(scriptSig == null) {
            return null;

        }
        return getAddressBase58(scriptSig.getPublicKey());
    }
}
