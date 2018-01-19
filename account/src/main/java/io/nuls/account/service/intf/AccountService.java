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
import io.nuls.core.chain.entity.NulsDigestData;
import io.nuls.core.chain.entity.NulsSignData;
import io.nuls.core.chain.entity.Result;

import java.util.List;

/**
 * @author Niels
 */
public interface AccountService {

    void init();

    void start();

    void shutdown();

    void destroy();

    Account createAccount();

    Result<List<String>> createAccount(int count);

    Account getDefaultAccount();

    Account getAccount(String address);

    boolean isMine(String address);

    List<Account> getAccountList();

    Address getAddress(String pubKey);

    byte[] getPrivateKey(String address);

    void setDefaultAccount(String id);

    Result encryptAccount(String password);

    Result changePassword(String oldPassword, String newPassword);

    boolean isEncrypted();

    Result unlockAccounts(String password, int seconds);

    NulsSignData signData(byte[] bytes);

    NulsSignData signData(NulsDigestData digestData);

    NulsSignData signData(byte[] bytes, String password);

    NulsSignData signData(NulsDigestData digestData, String password);

    NulsSignData signData(byte[] bytes, Account account, String password);

    NulsSignData signData(NulsDigestData digestData, Account account, String password);

    Result setAlias(String address, String password, String alias);

    Result verifySign(byte[] bytes, NulsSignData data);

    Result exportAccount(String filePath);

    Result exportAccount(String address, String filePath);

    Result exportAccounts(String filePath);

    Result importAccountsFile(String walletFilePath);
}
