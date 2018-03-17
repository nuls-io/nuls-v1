/**
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
package io.nuls.account.service.intf;

import io.nuls.account.entity.Account;
import io.nuls.account.entity.Address;
import io.nuls.account.entity.Alias;
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.entity.Result;
import io.nuls.core.exception.NulsException;

import java.util.List;

/**
 * @author Niels
 */
public interface AccountService {

    void init();

    void start();

    void shutdown();

    void destroy();

    Account createAccount(String passwd);

    Result<List<String>> createAccount(int count,String password);

    Result removeAccount(String address, String password);

    Account getDefaultAccount();

    Account getAccount(String address);

    boolean isMine(String address);

    List<Account> getAccountList();

    Address getAddress(String pubKey);

    Result getPrivateKey(String address, String password);

    void setDefaultAccount(String id);

    Result encryptAccount(String password);

    Result changePassword(String oldPassword, String newPassword);

    boolean isEncrypted();

    Result unlockAccounts(String password, int seconds);

    NulsSignData signDigest(byte[] bytes, byte[] priKey);

    NulsSignData signData(byte[] bytes, byte[] priKey);

    NulsSignData signDigest(NulsDigestData digestData, Account account, String password) throws NulsException;

    NulsSignData signDigest(byte [] digestBytes, Account account, String password) throws NulsException;

    NulsSignData signData(byte[] data, Account account, String password) throws NulsException;

    Result setAlias(String address, String password, String alias);

    Result verifySign(byte[] bytes, NulsSignData data, byte[] pubKey);

    Result verifyDigestSign(NulsDigestData digestData, NulsSignData signData,byte[] pubKey);

    Result exportAccount(String address, String password);

    Result exportAccounts(String password);

    Result importAccount(String priKey, String password);

    Result importAccounts(List<String> keys, String password);

    Alias getAlias(String address);
}
