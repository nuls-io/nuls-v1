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
 */

package io.nuls.account.service;

import io.nuls.account.model.Account;
import io.nuls.core.tools.model.Result;

import java.util.List;

/**
 * 账户模块提供给外部的服务接口定义
 * <p>
 * The account module provides the definition of the external service interface
 *
 * @author: Niels Wang
 * @date: 2018/5/4
 */
public interface AccountService {
    /**
     * 创建指定个数的账户（包含地址）
     * <p>
     * Create a specified number of accounts (including addresses)
     *
     * @param count    想要创建的账户个数
     * @param count    the account count you want to create
     * @param password the password of the wallet;
     * @return the result of the opration
     */
    Result<List<Account>> createAccount(int count, String password);

    /**
     * 根据账户标识删除对应的账户
     * <p>
     * Delete the corresponding account according to the account id.
     *
     * @param accountId the id of the account you want to delete;
     * @param password  the password of the wallet;
     * @return the result of the opration
     */
    Result<Boolean> deleteAccount(String accountId, String password);

    /**
     * 根据账户标识获取完成的账户信息
     * <p>
     * Obtain the completed account information according to the account id.
     *
     * @param accountId the id of the account you want ;
     * @return the operation result and the account entity
     */
    Result<Account> getAccount(String accountId);


}
