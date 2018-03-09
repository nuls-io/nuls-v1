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
package io.nuls.rpc.sdk.service;

import io.nuls.rpc.sdk.entity.BlockDto;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.JSONUtils;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/1
 */
public class WalletService {
    private RestFulUtils restFul = RestFulUtils.getInstance();

    public RpcClientResult transfer(String address, String password, String toAddress, Double amount, String remark) {
        Map<String,String> params = new HashMap<>();
    params.put("address",address);
    params.put("password",password);
    params.put("toAddress",toAddress);
    params.put("amount",amount+"");
    params.put("remark",remark);
        return this.restFul.post("/wallet/transfer", params);
    }

}
