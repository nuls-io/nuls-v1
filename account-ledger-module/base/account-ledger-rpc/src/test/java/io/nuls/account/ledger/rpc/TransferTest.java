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

package io.nuls.account.ledger.rpc;

import io.nuls.account.ledger.BaseTest;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.kernel.utils.AddressTool;

import java.util.ArrayList;
import java.util.List;

public class TransferTest extends BaseTest {
    private static List<String> list = new ArrayList<>();

    public static List<String> getAddressList() {
        if (list.isEmpty()) {
//            for (int i = 0; i < 100; i++) {
//                list.add(AddressTool.getStringAddressByBytes(AddressTool.getAddress(new ECKey().getPubKey())));
//            }
            list.add("NsdxMKjpm1Hp2FFmxXW1t9pwJ7JQXXxo");
        }
        return list;
    }

    private static int successCount = 0;

    public static void main(String[] args) {
        for (int i = 0; i < 1000000; i++) {
            doit();
        }
    }

    private static void doit() {
//        List<String> addressList = getAddressList();
//
//        for (String toAddress : addressList) {
            String address = "TTarACyVheCaPdGDwPv33yEGWvB35wdD";
            String toAddress = "TTakRBwcrXvn2EfapZnKhvHujGZhrTJK";//01385ef69371c8fe003d2339158333e6b383eaf7a93a38a77d022cf06024c82a
            long amount = 1001000L;
            String password = "";
            String remark = "test";

            String param = "{\"address\": \"" + address + "\", \"toAddress\": \"" + toAddress + "\", \"password\": \"" + password + "\", \"amount\": \"" + amount + "\", \"remark\": \"" + remark + "\"}";

            String url = "http://127.0.0.1:7001/api/accountledger/transfer";


            for (int i = 0; i < 1; i++) {
                String res = post(url, param, "utf-8");
                if (res.indexOf("true") != -1) {
                    successCount++;
                }
                System.out.println(successCount + "  " + res);
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//        }

    }

}
