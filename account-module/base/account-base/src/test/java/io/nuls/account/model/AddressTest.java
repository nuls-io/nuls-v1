/*
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
 *
 */

package io.nuls.account.model;

import io.nuls.core.tools.crypto.ECKey;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.kernel.context.NulsContext;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.utils.AddressTool;
import io.nuls.kernel.utils.SerializeUtils;
import org.junit.Test;

import java.math.BigInteger;

/**
 * @author: Niels Wang
 */
public class AddressTest {

    @Test
    public void test() {
        short chainId = 8964;
        NulsContext.getInstance().defaultChainId = chainId;
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(1,Hex.decode("60a958883bee99abc16b59a191ae25f8854c5edb2daea7ee1e19425b728edf6a")));
        System.out.println(ecKey.getPublicKeyAsHex(true));
//        while (true) {
//            ECKey ecKey = new ECKey();
//            String address = getAddress(chainId, ecKey.getPubKey());
//                System.out.println(address );//+ ":::::::" + ecKey.getPrivateKeyAsHex());
//        }
        System.out.println(getAddress(chainId, Hex.decode("0369926e4c82ab5499a14ef115e1d2d0d4dfb1c44b4de46434e79fc3da928d29ab")));
    }


    private String getAddress(short chainId, byte[] publicKey) {
        if (publicKey == null) {
            return null;
        }
        byte[] hash160 = SerializeUtils.sha256hash160(publicKey);
        Address address = new Address(chainId, (byte) 1, hash160);
        System.out.println(Hex.encode(address.getAddressBytes()));
        return address.getBase58();
    }


    private byte getXor(byte[] body) {
        byte xor = 0x00;
        for (int i = 0; i < body.length; i++) {
            xor ^= body[i];
        }

        return xor;
    }
}