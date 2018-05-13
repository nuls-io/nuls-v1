package io.nuls.account.storage.service;

import io.nuls.account.model.Address;
import io.nuls.account.storage.po.AccountPo;
import io.nuls.kernel.model.Result;

import java.util.List;

/**
 * 账户数据存储服务接口
 * Account data storage service interface
 *
 * @author: Charlie
 * @date: 2018/5/9
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

}
