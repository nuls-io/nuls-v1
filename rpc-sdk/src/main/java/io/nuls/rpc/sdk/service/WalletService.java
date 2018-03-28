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

import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.JSONUtils;
import io.nuls.rpc.sdk.utils.RestFulUtils;
import io.nuls.rpc.sdk.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/3/25
 */
public enum WalletService {
    WALLET_SERVICE;

    private RestFulUtils restFul = RestFulUtils.getInstance();

    /**
     * @param password
     * @return
     */
    @Deprecated
    public RpcClientResult setPassword(String password) {
        try {
            AssertUtil.canNotEmpty(password);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }

        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        String content = null;
        try {
            content = JSONUtils.obj2json(params);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.post("/wallet/encrypt", content);
    }

    /**
     * @param password
     * @param newpassword
     * @return
     */
    public RpcClientResult resetPassword(String password, String newpassword) {
        try {
            AssertUtil.canNotEmpty(password);
            AssertUtil.canNotEmpty(newpassword);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }

        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        params.put("newPassword", newpassword);
        String content = null;
        try {
            content = JSONUtils.obj2json(params);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.post("/wallet/reset", content);
    }

    /**
     * @param address
     * @param password
     * @param toAddress
     * @param amount
     * @param remark
     * @return
     */
    public RpcClientResult transfer(String address, String toAddress, Long amount, String password, String remark) {
        try {
            AssertUtil.canNotEmpty(address);
            AssertUtil.canNotEmpty(password);
            AssertUtil.canNotEmpty(toAddress);
            AssertUtil.canNotEmpty(amount);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        Map<String, String> params = new HashMap<>();
        params.put("address", address);
        params.put("password", password);
        params.put("toAddress", toAddress);
        params.put("amount", amount + "");
        params.put("remark", remark);
        String content = null;
        try {
            content = JSONUtils.obj2json(params);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return this.restFul.post("/wallet/transfer", content);
    }

    /**
     * @param password
     * @param address
     * @return
     */
    public RpcClientResult backup(String password, String address) {
        try {
            AssertUtil.canNotEmpty(password);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }

        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        if(StringUtils.isNotBlank(address)) {
            params.put("address", address);
        }
        String content = null;
        try {
            content = JSONUtils.obj2json(params);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.post("/wallet/backup", content);
    }

    /**
     * @param password
     * @param privateKey
     * @return
     */
    public RpcClientResult importAccount(String password, String privateKey) {
        try {
            AssertUtil.canNotEmpty(password);
            AssertUtil.canNotEmpty(privateKey);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }

        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        params.put("prikey", privateKey);
        String content = null;
        try {
            content = JSONUtils.obj2json(params);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.post("/wallet/import", content);
    }

    /**
     * @param address
     * @param password
     * @return
     */
    public RpcClientResult removeAccount(String address, String password) {
        try {
            AssertUtil.canNotEmpty(password);
            AssertUtil.canNotEmpty(address);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }

        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        params.put("address", address);
        String content = null;
        try {
            content = JSONUtils.obj2json(params);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.post("/wallet/remove", content);
    }

}
