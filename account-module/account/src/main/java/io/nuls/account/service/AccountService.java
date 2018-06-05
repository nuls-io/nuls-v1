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
import io.nuls.kernel.model.Na;
import io.nuls.kernel.model.NulsSignData;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * 账户模块提供给外部的服务接口定义
 * <p>
 * account service definition
 *
 * @author: Niels Wang
 * @date: 2018/5/4
 */
public interface AccountService {

    /**
     * 创建指定个数的账户（包含地址）
     * <p>
     * Create a specified number of accounts,and encrypt the accounts,
     * all the accounts are encrypted by the same password
     * if the password is NULL or "", the accounts will be unencrypted.
     *
     * @param count    想要创建的账户个数
     * @param count    the number of account you want to create.
     * @param password the password of the accounts.
     * @return the account list created.
     */
    Result<List<Account>> createAccount(int count, String password);

    /**
     * 创建指定个数的账户（包含地址）
     * Create unencrypted accounts.
     *
     * @param count 想要创建的账户个数
     * @param count the number of account you want to create.
     * @return the account list created.
     */
    Result<List<Account>> createAccount(int count);

    /**
     * 创建指定个数的账户（包含地址）
     * <p>
     * Create an account and encrypt it,
     * if the password is NULL or "", the accounts will be unencrypted.
     *
     * @param password the password of the accounts(only one account in the list).
     * @return the account list created.
     */
    Result<List<Account>> createAccount(String password);

    /**
     * 创建一个账户
     * <p>
     * Create an unencrypted account
     *
     * @return the account list created(only one account in the list).
     */
    Result<List<Account>> createAccount();

    /**
     * 根据账户标识删除对应的账户
     * <p>
     * delete an account by address.
     *
     * @param address  the address of the account you want to delete.
     * @param password the password of the account.
     * @return the result of the operation.
     */
    Result<Boolean> removeAccount(String address, String password);


    /**
     * 根据keyStore重置密码
     * <p>
     * Reset password by keyStore.
     *
     * @param keyStore the keyStore of the account.
     * @return the result of the operation.
     */
    Result<Account> updatePasswordByAccountKeyStore(AccountKeyStore keyStore, String password);

    /**
     * 从keyStore导入账户(密码用来验证keystore)
     * 1.从keyStore获取明文私钥(如果没有明文私钥,则通过密码从keyStore中的encryptedPrivateKey解出来)
     * 2.通过keyStore创建新账户,加密账户
     * 3.从数据库搜索此账户的别名,没有搜到则不设置(别名不从keyStore中获取,因为可能被更改)
     * 4.保存账户
     * 5.导入账户账本交易等信息
     * <p>
     * import an account form account key store.
     *
     * @param keyStore the keyStore of the account.
     * @return the result of the operation.
     */
    Result<Account> importAccountFormKeyStore(AccountKeyStore keyStore, String password);

    /**
     * 从keyStore导入账户
     * 1.从keyStore获取明文私钥
     * 2.通过keyStore创建新账户,不加密账户
     * 3.从数据库搜索此账户的别名,没有搜到则不设置(别名不从keyStore中获取,因为可能被更改)
     * 4.保存账户
     * 5.导入账户账本交易等信息
     * <p>
     * import an account form account key store.
     *
     * @param keyStore the keyStore of the account.
     * @return the result of the operation.
     */
    Result<Account> importAccountFormKeyStore(AccountKeyStore keyStore);


    /**
     * 根据私钥和密码导入账户
     * import an account from plant private key and encrypt the account.
     */
    Result<Account> importAccount(String prikey, String password);

    /**
     * 据私钥和密码导入账户
     * import an unencrypted account by plant private key.
     */
    Result<Account> importAccount(String prikey);

    /**
     * 导出账户到keyStore
     * <p>
     * export an account to an account key store.
     *
     * @param address  the address of the account.
     * @param password the password of the account key store.
     * @return the account key store object.
     */
    Result<AccountKeyStore> exportAccountToKeyStore(String address, String password);

    /**
     * 根据账户地址byte[]获取完整的账户信息
     * <p>
     * Query account information by address.
     *
     * @param address the address of the account you want to query.
     * @return the account.
     */
    Result<Account> getAccount(byte[] address);

    /**
     * 根据账户地址字符串获取完整的账户信息
     * <p>
     * Query account by address.
     *
     * @param address the address of the account you want to query.
     * @return the account.
     */
    Result<Account> getAccount(String address);

    /**
     * 根据账户地址类对象获取完整的账户信息
     * Query account by account address.
     *
     * @param address the address of the account you want to query;
     * @return the account.
     */
    Result<Account> getAccount(Address address);

    /**
     * 根据账户公钥获取账户地址对象
     * Query account address by public key.
     *
     * @param pubKey public key string.
     * @return the account address.
     */
    Result<Address> getAddress(String pubKey);

    /**
     * 根据账户二进制公钥获取账户地址对象
     * Gets the account address object from the account binary public key.
     *
     * @param pubKey public key binary array.
     * @return the account address.
     */
    Result<Address> getAddress(byte[] pubKey);

    /**
     * 根据账户验证账户是否加密
     * Verify weather the account is encrypted according to the account.
     *
     * @param account the account to be verified.
     * @return the result of the operation.
     */
    Result isEncrypted(Account account);

    /**
     * 根据账户的地址对象验证账户是否加密
     * Verify weather the account is encrypted according to the account's address object.
     *
     * @param address The address of the account to be verified.
     * @return the result of the operation.
     */
    Result isEncrypted(Address address);

    /**
     * 根据账户的地址字符串验证账户是否加密
     * Verify weather the account is encrypted according to the account's address string.
     *
     * @param address The address of the account to be verified.
     * @return the result of the operation.
     */
    Result isEncrypted(String address);

    /**
     * Verify the account password.
     */
    Result validPassword(Account account, String password);

    /**
     * 验证地址字符串的格式
     * Verify the format of the address string.
     *
     * @param address To verify the address string.
     * @return the result of the operation.
     */
    Result verifyAddressFormat(String address);

    /**
     * 获取所有账户集合
     * Query all account collections.
     *
     * @return account list of all accounts.
     */
    Result<List<Account>> getAccountList();

    /**
     * 数据签名
     * Sign data.
     *
     * @param data     Data to be signed.
     * @param account  Signed account
     * @param password Account password
     * @return The NulsSignData object.
     */
    NulsSignData signData(byte[] data, Account account, String password) throws NulsException;

    /**
     * 数据签名(无密码)
     * Sign data.(no password)
     *
     * @param data    Data to be signed.
     * @param account Signed account
     * @return The NulsSignData object.
     */
    NulsSignData signData(byte[] data, Account account) throws NulsException;

    /**
     * 数据签名
     * Sign data.
     *
     * @param data  Data to be signed.
     * @param ecKey eckey.
     * @return The NulsSignData object.
     */
    NulsSignData signData(byte[] data, ECKey ecKey) throws NulsException;

    /**
     * 数据签名
     * Sign data.
     *
     * @param digest   data digest.
     * @param account  account to sign.
     * @param password password of account.
     * @return the NulsSignData object.
     */
    NulsSignData signDigest(byte[] digest, Account account, String password) throws NulsException;

    /**
     * 数据签名
     * Sign data digest
     *
     * @param digest to be signed.
     * @param ecKey  eckey
     * @return The NulsSignData object.
     */
    NulsSignData signDigest(byte[] digest, ECKey ecKey);

    /**
     * 验证签名
     * Verify the signature.
     *
     * @param data     data to be validated.
     * @param signData signature.
     * @param pubKey   dublic key of account.
     * @return the result of the opration
     */
    Result verifySignData(byte[] data, NulsSignData signData, byte[] pubKey);

    /**
     * 获取所有的账户的余额 ?
     * Query the balance of all accounts.
     *
     * @return Balance object.
     */
    Result<Balance> getBalance() throws NulsException;

    /**
     * 根据账户获取账户余额
     * Query the balance of an account.
     *
     * @param account the account.
     * @return Balance object.
     */
    Result<Balance> getBalance(Account account) throws NulsException;

    /**
     * 根据账户地址对象获取账户余额
     * Query the balance of an account.
     *
     * @param address the address of the account.
     * @return Balance object.
     */
    Result<Balance> getBalance(Address address) throws NulsException;

    /**
     * 根据账户地址字符串获取账户余额
     * Query the balance of an account.
     *
     * @param address the address of the account.
     * @return Balance object.
     */
    Result<Balance> getBalance(String address) throws NulsException;


    /**
     * 根据账户地址字节数组获取账户别名
     * Get an account alias based on the array of account address bytes
     * @param address
     * @return alias string
     */
    Result<String> getAlias(byte[] address);

    /**
     * 根据账户地址获取账户别名
     * Get account alias according to account address
     * @param address
     * @return alias string
     */
    Result<String> getAlias(String address);


    Result<Na> getAliasFee(String addr, String aliasName);
}
