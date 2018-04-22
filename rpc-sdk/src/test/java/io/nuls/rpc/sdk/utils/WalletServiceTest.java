/*
 *
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

package io.nuls.rpc.sdk.utils;

import io.nuls.core.utils.log.Log;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.service.WalletService;
import io.nuls.rpc.sdk.utils.RestFulUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: Niels Wang
 * @date: 2018/4/15
 */
public class WalletServiceTest {

//    @BeforeClass
    public static void init() {
        RestFulUtils.getInstance().setServerUri("http://127.0.0.1:8001");
    }

    WalletService walletService = WalletService.WALLET_SERVICE;

//    @Test
    public void transfer() {
        int index = 0;
        while (index++ < 10000) {
            RpcClientResult result = walletService.transfer("2Cg2Tz4mD3XmkGJUA9b49k3NSbQm5Ca", "2Ciw3E1fhMLZZg8v7MLKTWQ2XWEVka1", 1286L, "nuls123456", "test utxo");
            System.out.println(result.getMsg()+"===="+index);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }

        assertTrue(true);
    }
}