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

package io.nuls.account.storage.service;

import io.nuls.kernel.model.Address;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * 账户数据存储服务接口
 * Account data storage service interface
 *
 * @author: Charlie
 */
public interface AccountStorageService {

    /**
     * 创建账户多个账户
     * Create accounts
     * @param accountPoList 待创建的账户集合
     * @param accountPoList Account collection to be created
     * @return the result of the opration
     */
    Result saveAccountList(List<AccountPo> accountPoList);

    /**
     * 创建账户
     * Create account
     * @param account
     * @return
     */
    Result saveAccount(AccountPo account);

    /**
     * 删除账户
     * Delete account
     * @param address Account address to be deleted
     * @return the result of the opration
     */
    Result removeAccount(Address address);

    /**
     * 获取所有账户
     * @return the result of the opration and Result<List<Account>>
     */
    Result<List<AccountPo>> getAccountList();

    /**
     * 根据账户获取账户信息
     * According to the account to obtain account information
     * @param address
     * @return the result of the opration
     */
    Result<AccountPo> getAccount(Address address);

    /**
     * 根据账户获取账户信息
     * According to the account to obtain account information
     * @param address
     * @return the result of the opration
     */
    Result<AccountPo> getAccount(byte[] address);


    /**
     * 根据账户更新账户信息
     * Update account information according to the account.
     * @param account The account to be updated.
     * @return the result of the opration
     */
    Result updateAccount(AccountPo account);

    /**
     * 保存默认账户
     * Set default account
     * @param account default
     * @return
     */
    Result saveDefaultAccount(AccountPo account);

    /**
     * 获取默认账户
     * get default account
     * @return
     */
    Result<AccountPo> getDefaultAccount();

    /**
     * remove default account
     * @return
     */
    Result removeDefaultAccount();
}
