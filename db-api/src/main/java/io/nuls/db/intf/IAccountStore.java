package io.nuls.db.intf;

import io.nuls.db.entity.Account;

import java.util.List;

/**
 * Created by win10 on 2017/10/20.
 */
public interface IAccountStore extends IStore<Account, String>{

    /**
     * 通过地址获取本地账户
     * @param address
     * @return
     */
    Account getLocalAccountByKey(String address);


    /**
     * 获取本地账户列表
     * @return
     */
    List<Account> getLocalAccountList();

    /**
     * 检查账户是否存在
     * @param address
     * @return
     */
    Boolean exist(String address);
}
