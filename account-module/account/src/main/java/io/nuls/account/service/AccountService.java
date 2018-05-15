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
import io.nuls.account.model.AccountKeyStore;
import io.nuls.account.model.Address;
import io.nuls.account.model.Balance;
import io.nuls.core.tools.crypto.ECKey;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.model.NulsSignData;
import io.nuls.kernel.model.Result;

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
     * 创建指定个数的账户（包含地址）
     * Create a specified number of accounts (including addresses)
     *
     * @param count 想要创建的账户个数
     * @param count the account count you want to create
     * @return
     */
    Result<List<Account>> createAccount(int count);

    /**
     * 创建指定个数的账户（包含地址）
     * <p>
     * Create a specified number of accounts (including addresses)
     *
     * @param password the password of the wallet;
     * @return the result of the opration
     */
    Result<List<Account>> createAccount(String password);

    /**
     * 创建一个账户
     * <p>
     * Create a accounts
     *
     * @return the result of the opration
     */
    Result<List<Account>> createAccount();

    /**
     * 根据账户标识删除对应的账户
     * <p>
     * remove the corresponding account according to the account id.
     *
     * @param address  the address of the account you want to delete;
     * @param password the password of the wallet;
     * @return the result of the opration
     */
    Result<Boolean> removeAccount(String address, String password);

    /**
     * 从keyStore导入账户
     * <p>
     * import an account form account key store.
     *
     * @param keyStore the keyStore of the account;
     * @return the result of the opration
     */
    Result<Account> importAccountFormKeyStore(AccountKeyStore keyStore);

    /**
     * 导出账户到keyStore
     * <p>
     * export an account to an account key store.
     *
     * @param address  the address of the account;
     * @param password the password of the account key store;
     * @return the account key store object
     */
    Result<AccountKeyStore> exportAccountToKeyStore(String address, String password);

    /**
     * 根据账户地址byte[]获取完整的账户信息
     * <p>
     * Get the full account information based on the account address string.
     *
     * @param address the address of the account you want ;
     * @return the operation result and the account model
     */
    Result<Account> getAccount(byte[] address);

    /**
     * 根据账户地址字符串获取完整的账户信息
     * <p>
     * Get the full account information based on the account address string.
     *
     * @param address the address of the account you want ;
     * @return the operation result and the account model
     */
    Result<Account> getAccount(String address);

    /**
     * 根据账户地址类对象获取完整的账户信息
     * Get the full account information according to the account address object.
     *
     * @param address The address object of the account you want;
     * @return the operation result and the account model
     */
    Result<Account> getAccount(Address address);

    /**
     * 根据账户公钥获取账户地址对象
     * Get the account address object from the account public key.
     *
     * @param pubKey Public key string
     * @return the operation result and the address model
     */
    Result<Address> getAddress(String pubKey);

    /**
     * 根据账户二进制公钥获取账户地址对象
     * Gets the account address object from the account binary public key.
     *
     * @param pubKey Public key binary array.
     * @return the operation result and the address model
     */
    Result<Address> getAddress(byte[] pubKey);

    /**
     * 根据账户验证账户是否加密
     * Verify that the account is encrypted according to the account.
     *
     * @param account The account to be verified.
     * @return the result of the opration
     */
    Result isEncrypted(Account account);

    /**
     * 根据账户的地址对象验证账户是否加密
     * Verify that the account is encrypted according to the account's address object.
     *
     * @param address The address object of the account to be verified.
     * @return the result of the opration
     */
    Result isEncrypted(Address address);

    /**
     * 根据账户的地址字符串验证账户是否加密
     * Verify that the account is encrypted according to the account's address string.
     *
     * @param address The address string of the account to be verified.
     * @return the result of the opration
     */
    Result isEncrypted(String address);

    /**
     * Verify the account password is correct
     * @param account
     * @param password
     * @return
     */
    Result validPassword(Account account, String password);

    /**
     * 验证地址字符串的格式
     * Verify the format of the address string.
     *
     * @param address To verify the address string.
     * @return the result of the opration
     */
    Result verifyAddressFormat(String address);

    /**
     * 获取所有账户集合
     * Get all account collections.
     *
     * @return the result of the opration and the account list
     */
    Result<List<Account>> getAccountList();

    /**
     * 数据签名
     * The data signature
     *
     * @param data     Data to be signed.
     * @param account  Signed account
     * @param password Account password
     * @return The NulsSignData object after the signature.
     * @throws NulsException
     */
    NulsSignData signData(byte[] data, Account account, String password) throws NulsException;

    /**
     * 数据签名(无密码)
     * The data signature(no password)
     *
     * @param data    Data to be signed.
     * @param account Signed account
     * @return The NulsSignData object after the signature.
     * @throws NulsException
     */
    NulsSignData signData(byte[] data, Account account) throws NulsException;

    /**
     * 数据签名
     * The data signature
     *
     * @param data  Data to be signed.
     * @param ecKey eckey
     * @return The NulsSignData object after the signature.
     * @throws NulsException
     */
    NulsSignData signData(byte[] data, ECKey ecKey) throws NulsException;

    /**
     * 数据签名
     * The data signature
     * @param digest
     * @param account
     * @param password
     * @return
     * @throws NulsException
     */
    NulsSignData signDigest(byte[] digest, Account account, String password) throws NulsException;

    /**
     * 数据签名
     * The data signature
     *
     * @param digest to be signed.
     * @param ecKey eckey
     * @return The NulsSignData object after the signature.
     * @throws NulsException
     */
    NulsSignData signDigest(byte[] digest, ECKey ecKey);

    /**
     * 验证签名
     * Verify the signature
     *
     * @param data     Data to be validated.
     * @param signData Signed data.
     * @param pubKey   Public key of account
     * @return the result of the opration
     */
    Result verifySignData(byte[] data, NulsSignData signData, byte[] pubKey);

    /**
     * 获取所有的账户的余额 ?
     * Obtain the balance of all accounts.
     *
     * @return the result of the opration and the balance model
     */
    Result<Balance> getBalance() throws NulsException;

    /**
     * 根据账户获取账户余额
     *
     * @param account The account to which the balance is to be obtained.
     * @return the result of the opration and the balance model
     */
    Result<Balance> getBalance(Account account) throws NulsException;

    /**
     * 根据账户地址对象获取账户余额
     *
     * @param address The address object of the account to which the balance is to be obtained.
     * @return the result of the opration and the balance model
     */
    Result<Balance> getBalance(Address address) throws NulsException;

    /**
     * 根据账户地址字符串获取账户余额
     *
     * @param address The address string of the account to which the balance is to be obtained.
     * @return the result of the opration and the balance model
     */
    Result<Balance> getBalance(String address) throws NulsException;
}
