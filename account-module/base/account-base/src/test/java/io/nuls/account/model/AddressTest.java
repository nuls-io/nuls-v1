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
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.SerializeUtils;
import org.junit.Test;

/**
 * @author: Niels Wang
 * @date: 2018/5/5
 */
public class AddressTest {

    @Test
    public void test() {
        final short chainId = 16402;
        while (true) {
            ECKey ecKey = new ECKey();
            String address = getAddress(chainId, ecKey.getPubKey());
//            if (address.toUpperCase().startsWith("NS")) {
                System.out.println(chainId + "::::::::::" + address);
//            } else if (address.toUpperCase().startsWith("N")) {
//                System.out.println(chainId + "::::::::::" + address);
//            }
        }
    }


    private String getAddress(short chainId, byte[] publicKey) {
        if (publicKey == null) {
            return null;
        }
        byte[] hash160 = SerializeUtils.sha256hash160(publicKey);
        byte[] body = new byte[22];
        System.arraycopy(SerializeUtils.shortToBytes(chainId), 0, body, 0, 2);
        System.arraycopy(hash160, 0, body, 2, hash160.length);
        byte xor = getXor(body);
        byte[] base58bytes = new byte[23];
        System.arraycopy(body, 0, base58bytes, 0, body.length);
        base58bytes[body.length] = xor;
        return Base58.encode(base58bytes);
    }


    private byte getXor(byte[] body) {
        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }

        return xor;
    }
}