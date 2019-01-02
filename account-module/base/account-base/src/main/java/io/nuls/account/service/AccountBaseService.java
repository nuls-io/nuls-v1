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

package io.nuls.account.service;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.Account;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.account.storage.service.AccountStorageService;
import io.nuls.core.tools.crypto.Hex;
import io.nuls.core.tools.log.Log;
import io.nuls.core.tools.str.StringUtils;
import io.nuls.kernel.exception.NulsException;
import io.nuls.kernel.lite.annotation.Autowired;
import io.nuls.kernel.lite.annotation.Service;
import io.nuls.kernel.model.Result;
import io.nuls.kernel.utils.AddressTool;

import java.util.*;


/**
 * @author: Charlie
 */
@Service
public class AccountBaseService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountStorageService accountStorageService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    public Result setRemark(String address, String remark){
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (StringUtils.isBlank(remark)) {
            remark = null;
        }
        if (!StringUtils.validRemark(remark)) {
            return Result.getFailed(AccountErrorCode.NICKNAME_TOO_LONG);
        }
        account.setRemark(remark);
        Result result = accountStorageService.updateAccount(new AccountPo(account));
        if (result.isFailed()) {
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        return Result.getSuccess().setData(true);
    }

    /**
     * 获取账户私钥
     * Get the account private key
     *
     * @param address
     * @param password
     * @return
     */
    public Result getPrivateKey(String address, String password) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        //加过密(有密码) 就验证密码 Already encrypted(Added password) and did not unlock, verify password
        if (account.isEncrypted()) {
            try {
                byte[] priKeyBytes = priKeyBytes = account.getPriKey(password);
                return Result.getSuccess().setData(Hex.encode(priKeyBytes));
            } catch (NulsException e) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        return Result.getFailed(AccountErrorCode.ACCOUNT_UNENCRYPTED);
    }

    /**
     * 获取所有本地账户账户私钥，必须保证所有账户密码一致，
     * 如果本地账户中的密码不一致，将返回错误信息
     * Get the all local private keys
     *
     * @param password
     * @return
     */
    public Result getAllPrivateKey(String password) {
        Collection<Account> localAccountList = accountService.getAccountList().getData();
        if (localAccountList == null || localAccountList.isEmpty()) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (StringUtils.isNotBlank(password) && !StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        List<String> list = new ArrayList<>();
        for (Account account : localAccountList) {
            if (account.isEncrypted()){
                if(StringUtils.isBlank(password)){
                    //如果有账户是加密的，但是没有传密码,则返回错误信息；
                    return Result.getFailed(AccountErrorCode.HAVE_ENCRYPTED_ACCOUNT);
                }
                try {
                    byte[] priKeyBytes = account.getPriKey(password);
                    list.add(Hex.encode(priKeyBytes));
                } catch (NulsException e) {
                    return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }else {
                if (StringUtils.isNotBlank(password)) {
                    //账户集合中有未加密账户，但是参数传了密码
                    return Result.getFailed(AccountErrorCode.HAVE_UNENCRYPTED_ACCOUNT);
                }
                list.add(Hex.encode(account.getPriKey()));
            }
        }
        Map<String, List<String>> map = new HashMap<>();
        map.put("value", list);
        return Result.getSuccess().setData(map);
    }

    public Result getAllPrivateKey() {
        return getAllPrivateKey(null);
    }


    /**
     * 设置密码
     * Set password (Encryption account)
     *
     * @param address
     * @param password
     * @return
     */
    public Result setPassword(String address, String password) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(password)) {
            return Result.getFailed(AccountErrorCode.NULL_PARAMETER);
        }
        if (!StringUtils.validPassword(password)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG);

        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (account.isEncrypted()) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_IS_ALREADY_ENCRYPTED);
        }
        try {
            account.encrypt(password);
            Result result = accountStorageService.updateAccount(new AccountPo(account));
            if (result.isFailed()) {
                return Result.getFailed(AccountErrorCode.FAILED);
            }
            accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(AccountErrorCode.FAILED);
        }
        return Result.getSuccess().setData(true);
    }

    /**
     * 根据原密码修改账户密码
     * Change the account password according to the current password
     *
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public Result changePassword(String address, String oldPassword, String newPassword) {
        if (!AddressTool.validAddress(address)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(oldPassword)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (StringUtils.isBlank(newPassword)) {
            return Result.getFailed(AccountErrorCode.PARAMETER_ERROR);
        }
        if (!StringUtils.validPassword(oldPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }
        if (!StringUtils.validPassword(newPassword)) {
            return Result.getFailed(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }
        Account account = accountService.getAccount(address).getData();
        if (null == account) {
            return Result.getFailed(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        try {
            if (!account.isEncrypted()) {
                return Result.getFailed(AccountErrorCode.ACCOUNT_UNENCRYPTED);
            }
            if (!account.validatePassword(oldPassword)) {
                return Result.getFailed(AccountErrorCode.PASSWORD_IS_WRONG);
            }
            account.unlock(oldPassword);
            account.encrypt(newPassword, true);
            AccountPo po = new AccountPo(account);
            Result result = accountStorageService.updateAccount(po);
            if (result.isFailed()) {
                return Result.getFailed(AccountErrorCode.FAILED);
            }
            accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            return result.setData(true);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

}
