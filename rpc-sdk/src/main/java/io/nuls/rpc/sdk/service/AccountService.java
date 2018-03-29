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

import io.nuls.rpc.sdk.entity.*;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.JSONUtils;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Desription:
 * @Author: PierreLuo
 * @Date: 2018/3/25
 */
public enum AccountService {
    ACCOUNT_SERVICE;
    private RestFulUtils restFul = RestFulUtils.getInstance();
    {

    }

    /**
     * @param password : the password of the walletl
     * @param count    : how many acounts you want create;
     * @return
     */
    public RpcClientResult create(String password, Integer count) {
        try {
            AssertUtil.canNotEmpty(password);
            AssertUtil.canNotEmpty(count);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        params.put("count", count + "");
        String content = null;
        try {
            content = JSONUtils.obj2json(params);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.post("/account", content);
    }

    /**
     * @param address,the address string of the account
     * @return account info
     */
    public RpcClientResult getBaseInfo(String address) {
        try {
            AssertUtil.canNotEmpty(address);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        RpcClientResult result = restFul.get("/account/" + address, null);
        if (result.isSuccess()) {
            result.setData(new AccountDto((Map<String, Object>) result.getData()));
        }
        return result;
    }

    /**
     * get the balance items of the address
     *
     * @param address can not null
     * @return
     */
    private RpcClientResult getBalanceBase(String address) {
        try {
            AssertUtil.canNotEmpty(address);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.get("/account/balance/" + address, null);
    }

    public RpcClientResult getBalance(String address) {
        RpcClientResult result = getBalanceBase(address);
        if (result.isSuccess()) {
            result.setData(new BalanceDto((Map<String, Object>) result.getData()));
        }
        return result;
    }

    public RpcClientResult getBalanceNa2Nuls(String address) {
        RpcClientResult result = getBalanceBase(address);
        if (result.isSuccess()) {
            result.setData(new BalanceNa2NulsDto((Map<String, Object>) result.getData()));
        }
        return result;
    }

    /**
     * get total balance
     *
     * @return
     */
    private RpcClientResult getTotalBalanceBase() {
        return restFul.get("/account/balances", null);
    }
    public RpcClientResult getTotalBalance() {
        RpcClientResult result = getTotalBalanceBase();
        if (result.isSuccess()) {
            result.setData(new BalanceDto((Map<String, Object>) result.getData()));
        }
        return result;
    }
    public RpcClientResult getTotalBalanceNa2Nuls() {
        RpcClientResult result = getTotalBalanceBase();
        if (result.isSuccess()) {
            result.setData(new BalanceNa2NulsDto((Map<String, Object>) result.getData()));
        }
        return result;
    }

    /**
     * get all local accounts
     *
     * @return
     */
    public RpcClientResult getAccountList() {
        RpcClientResult result = restFul.get("/account/list", null);
        if (result.isSuccess()) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
            List<AccountDto> accountDtoList = new ArrayList<>();
            for (Map<String, Object> map : list) {
                accountDtoList.add(new AccountDto(map));
            }
            result.setData(accountDtoList);
        }
        return result;
    }


    /**
     * @param address
     * @param amount
     * @return
     */
    private RpcClientResult getUTXOBase(String address, Long amount) {
        try {
            AssertUtil.canNotEmpty(address);
            AssertUtil.canNotEmpty(amount);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }

        Map<String, String> params = new HashMap<>();
        params.put("address", address);
        params.put("amount", amount.toString());
        return restFul.get("/account/utxo", params);
    }
    public RpcClientResult getUTXO(String address, Long amount) {
        RpcClientResult result = getUTXOBase(address, amount);
        if (result.isSuccess()) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
            List<OutputDto> outputDtoList = new ArrayList<>();
            for (Map<String, Object> map : list) {
                outputDtoList.add(new OutputDto(map));
            }
            result.setData(outputDtoList);
        }
        return result;
    }
    public RpcClientResult getUTXONa2Nuls(String address, Long amount) {
        RpcClientResult result = getUTXOBase(address, amount);
        if (result.isSuccess()) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
            List<OutputNa2NulsDto> outputNa2NulsDtoList = new ArrayList<>();
            for (Map<String, Object> map : list) {
                outputNa2NulsDtoList.add(new OutputNa2NulsDto(map));
            }
            result.setData(outputNa2NulsDtoList);
        }
        return result;
    }

    /**
     * @param alias,    the name you want set to the address
     * @param address,  what you want set a name
     * @param password, the password of the wallet
     * @return
     */
    public RpcClientResult setAlias(String alias, String address, String password) {
        try {
            AssertUtil.canNotEmpty(alias);
            AssertUtil.canNotEmpty(address);
            AssertUtil.canNotEmpty(password);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }

        Map<String, String> params = new HashMap<>();
        params.put("alias", alias);
        params.put("address", address);
        params.put("password", password);
        String content = null;
        try {
            content = JSONUtils.obj2json(params);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.post("/account/alias", content);
    }

    /**
     * @param address
     * @param password
     * @return
     */
    public RpcClientResult getPrivateKey(String address, String password) {
        try {
            AssertUtil.canNotEmpty(address);
            AssertUtil.canNotEmpty(password);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }

        Map<String, String> params = new HashMap<>();
        params.put("address", address);
        params.put("password", password);
        String content = null;
        try {
            content = JSONUtils.obj2json(params);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }
        return restFul.post("/account/prikey", content);
    }

    /**
     *
     *
     * @param address
     * @return
     */
    private RpcClientResult getAssetBase(String address) {
        try {
            AssertUtil.canNotEmpty(address);
        } catch (Exception e) {
            return RpcClientResult.getFailed(e.getMessage());
        }

        return restFul.get("/account/assets/" + address, null);
    }
    public RpcClientResult getAsset(String address) {
        RpcClientResult result = getAssetBase(address);
        if (result.isSuccess()) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
            List<AssetDto> assetDtoList = new ArrayList<>();
            for (Map<String, Object> map : list) {
                assetDtoList.add(new AssetDto(map));
            }
            result.setData(assetDtoList);
        }
        return result;
    }
    public RpcClientResult getAssetNa2Nuls(String address) {
        RpcClientResult result = getAssetBase(address);
        if (result.isSuccess()) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) result.getData();
            List<AssetNa2NulsDto> assetNa2NulsDtoList = new ArrayList<>();
            for (Map<String, Object> map : list) {
                assetNa2NulsDtoList.add(new AssetNa2NulsDto(map));
            }
            result.setData(assetNa2NulsDtoList);
        }
        return result;
    }


}
