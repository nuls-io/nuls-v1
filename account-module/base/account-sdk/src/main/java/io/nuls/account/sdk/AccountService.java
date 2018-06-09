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

package io.nuls.account.sdk;

import io.nuls.account.sdk.model.AccountKeyStoreDto;
import io.nuls.sdk.model.Result;

/**
 * @author: Charlie
 * @date: 2018/6/8
 */
public interface AccountService {

    /**
     * Create an unencrypted account
     *
     * @return Result
     * If the operation is successful, 'success' is true, and data is AccountDto Object;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createAccount();

    /**
     * Create an encrypted account
     *
     * @param password The password of the account
     * @return Result
     * If the operation is successful, 'success' is true and data is AccountDto Object;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createAccount(String password);

    /**
     * Create unencrypted accounts
     *
     * @param count The number of accounts you want to create
     * @return Result
     * If the operation is successful, 'success' is true and data is AccountDto Object;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createAccount(int count);

    /**
     * Create encrypted accounts
     *
     * @param count The number of accounts you want to create
     * @return Result
     * If the operation is successful, 'success' is true and data is AccountDto Object;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createAccount(int count, String password);


    /**
     * Create an unencrypted local account (Not saved to the database)
     *
     * @return Result
     * If the operation is successful, 'success' is true, and data is AccountDto Object;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createLocalAccount();

    /**
     * Create an encrypted local account (Not saved to the database)
     *
     * @param password The password of the account
     * @return Result
     * If the operation is successful, 'success' is true and data is AccountDto Object;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createLocalAccount(String password);

    /**
     * Create unencrypted local accounts (Not saved to the database)
     *
     * @param count The number of accounts you want to create
     * @return Result
     * If the operation is successful, 'success' is true and data is AccountDto Object;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createLocalAccount(int count);

    /**
     * Create encrypted local accounts (Not saved to the database)
     *
     * @param count The number of accounts you want to create
     * @return Result
     * If the operation is successful, 'success' is true and data is AccountDto Object;
     * If the operation fails, "success" is false and the result has error information
     */
    Result createLocalAccount(int count, String password);


    Result backupAccount(String address, String password);

    /**
     * Get the fee for setting the alias (The fee don't include the fixed 1 NULS to be destroyed)
     * @param address The address of account to set an alias for
     * @param alias The alias value to be set
     * @return
     * If the operation is successful, 'success' is true and data is fee;
     * If the operation fails, "success" is false and the result has error information
     */
    Result getAliasFee(String address, String alias);

    Result getAccount(String address);

    Result getAccountList(int pageNUmber, int pageSize);


    Result getAssets(String address);
    Result getAddressByAlias(String aliasName);

    Result getPrikey(String address, String password);
    Result getLocalTotalBalance();

    Result isAliasExist(String aliasName);

    //overwrite:true if the account exists, it will not be executed
    Result importAccountByKeystore(AccountKeyStoreDto accountKeyStoreDto, boolean overwrite, String password);

    Result importAccountByPriKey(String privateKey, boolean overwrite, String password);


    /**
     * set alias
     * @param address The address of account to set an alias for
     * @param alias The alias value to be set
     * @param password The password of account, this parameter can be passed null if the account is unencrypted
     * @return
     * If the operation is successful, 'success' is true;
     * If the operation fails, "success" is false and the result has error information
     */
    Result setAlias(String address, String alias, String password);

}
