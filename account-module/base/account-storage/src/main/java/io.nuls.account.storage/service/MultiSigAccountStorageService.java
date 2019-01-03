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

package io.nuls.account.storage.service;

import io.nuls.account.storage.po.AccountPo;
import io.nuls.kernel.model.Address;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * 账户数据存储服务接口
 * Account data storage service interface
 *
 * @author: Charlie
 */
public interface MultiSigAccountStorageService {

    /**
     * 创建账户
     * save account
     * @return
     */
    Result saveAccount(Address address,byte[] multiSigAccount);

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
    Result<List<byte[]>> getAccountList();

    /**
     * 根据账户获取账户信息
     * According to the account to obtain account information
     * @param address
     * @return the result of the opration
     */
    Result<byte[]> getAccount(Address address);

}
