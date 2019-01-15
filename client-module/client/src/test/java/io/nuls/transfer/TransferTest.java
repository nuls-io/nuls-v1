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
package io.nuls.transfer;

import io.nuls.kernel.model.CommandResult;
import io.nuls.kernel.model.RpcClientResult;
import io.nuls.kernel.utils.RestFulUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author: Charlie
 * @date: 2018/7/8
 */
public class TransferTest implements Runnable {
    private String from;
    private List<String> to;
    private RestFulUtils restFul = TestMain.restFul;

    public TransferTest(String from,List<String> to){
        this.from = from;
        this.to = to;
    }

    @Override
    public void run() {
        int count = 0;
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (String toAdd : to) {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("address", from);
                parameters.put("toAddress", toAdd);
                parameters.put("password", null);
                parameters.put("amount", 300000000);
                parameters.put("remark", Thread.currentThread().getName());
                RpcClientResult result = restFul.post("/accountledger/transfer", parameters);
                if (result.isFailed()) {
                    System.out.println(Thread.currentThread().getName() + " - transfer fail : " + (++count));
                } else {
                    System.out.println(Thread.currentThread().getName() + " - transfer success : " + (++count));
                }
            }
        }
    }

    public static void main(String[] args) {
        String a = "02230020a2075608eb8b0468413e0537d909196dc16ad1567f5b957237e68ce9deba21e40000204aa9d1010000ffffffffffff230020407969ea49a0f74c5874c725296a6043c30391293fc56f8327201841f80ef65b000080ca3961240000ffffffffffff0217042301dbdb74f285112040dc80c56fe086c16e192d4e9700204aa9d10100007f2dd4d3660117042301dbdb74f285112040dc80c56fe086c16e192d4e970080ca3961240000000000000000";
        String b = "02230020a2075608eb8b0468413e0537d909196dc16ad1567f5b957237e68ce9deba21e40000204aa9d1010000ffffffffffff230020407969ea49a0f74c5874c725296a6043c30391293fc56f8327201841f80ef65b000080ca3961240000ffffffffffff0217042301dbdb74f285112040dc80c56fe086c16e192d4e9700204aa9d1010000fa33d6d3660117042301dbdb74f285112040dc80c56fe086c16e192d4e970080ca3961240000000000000000";
        System.out.println(a.equals(b));
    }
}
