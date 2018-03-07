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

import io.nuls.rpc.sdk.entity.AccountDto;
import io.nuls.rpc.sdk.entity.RpcClientResult;
import io.nuls.rpc.sdk.utils.AssertUtil;
import io.nuls.rpc.sdk.utils.JSONUtils;
import io.nuls.rpc.sdk.utils.RestFulUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 * @date 2017/11/1
 */
public class AccountService {
    private RestFulUtils restFul = RestFulUtils.getInstance();

    /**
     * @param password : the password of the walletl
     * @param count    : how many acounts you want create;
     * @return
     */
    public RpcClientResult create(String password, Integer count) {
        AssertUtil.canNotEmpty(password);
        AssertUtil.canNotEmpty(count);
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
    public RpcClientResult load(String address) {
        AssertUtil.canNotEmpty(address);
        //todo 路径应该去掉get
        RpcClientResult result = restFul.get("/account/get/" + address, null);
        if (result.isSuccess()) {
            result.setData(new AccountDto((Map<String, Object>) result.getData()));
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
        AssertUtil.canNotEmpty(alias);
        AssertUtil.canNotEmpty(address);
        AssertUtil.canNotEmpty(password);
        //todo 等待接口修改后实现


        return null;
    }

    /**
     * get all local accounts
     *
     * @return
     */
    public RpcClientResult getAccountList(){
        RpcClientResult result = restFul.get("/account/list",null);
        if(result.isSuccess()){
            List<Map<String ,Object> > list = (List<Map<String, Object>>) result.getData();
            List<AccountDto> accountDtoList = new ArrayList<>();
            for(Map<String,Object> map:list){
                accountDtoList.add(new AccountDto(map));
            }
            result.setData(accountDtoList);
        }
        return result;
    }

    /**
     * get the balance items of the address
     * @param address can not null
     * @return
     */
    public RpcClientResult getBalance(String address){
        AssertUtil.canNotEmpty(address);
        RpcClientResult result = restFul.get("/account/balance/"+address,null);
        //todo 接口需要修改
        return null;
    }
}
